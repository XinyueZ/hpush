package pushmessager

import (
	"appengine"
	"appengine/urlfetch"

	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"strconv"
	"time"
	"strings"
	"net/url"
)

type TopStoresRes struct {
	Collection []int64
}

type ItemDetails struct {
	By    string  `by`
	Id    int64   `id`
	Kids  []int64 `kids`
	Score int64   `score`
	Text  string  `text`
	Time  int64   `time`
	Title string  `title`
	Type  string  `type`
	Url   string  `url`
}

type SyncItemDetails struct {
	By          string
	Id          int64
	Kids        int
	Score       int64
	Text        string
	Time        int64
	Title       string
	Type        string
	Url         string
	Pushed_Time string
}

type SyncItemDetailsList struct {
	SyncList []SyncItemDetails
}

//Get list of all "ids" of items.
func getTopStories(w http.ResponseWriter, r *http.Request, api string) []int64 {
	apiUrl := API_HOST + API_VERSION + "/" + api + ".json"
	cxt := appengine.NewContext(r)
	if req, err := http.NewRequest(API_METHOD, apiUrl, nil); err == nil {
		httpClient := urlfetch.Client(cxt)
		r, err := httpClient.Do(req)
		defer r.Body.Close()
		if err == nil {
			if bytes, err := ioutil.ReadAll(r.Body); err == nil {
				topStoresRes := make([]int64, 0)
				json.Unmarshal(bytes, &topStoresRes)
				return topStoresRes
			} else {
				cxt.Errorf("getTopStories unmarshal: %v", err)
			}
		} else {
			cxt.Errorf("getTopStories doing: %v", err)
		}
	} else {
		cxt.Errorf("getTopStories: %v", err)
	}
	return nil
}

//Load detail of item.
func loadItemDetail(w http.ResponseWriter, r *http.Request, itemId int64, detailsList *[]*ItemDetails, ch chan int) {
	api := fmt.Sprintf(API_GET_ITEM_DETAILS, itemId)
	cxt := appengine.NewContext(r)
	if req, err := http.NewRequest(API_METHOD, api, nil); err == nil {
		httpClient := urlfetch.Client(cxt)
		res, err := httpClient.Do(req)
		if res != nil {
			defer res.Body.Close()
		}
		if err == nil {
			if bytes, err := ioutil.ReadAll(res.Body); err == nil {
				details := new(ItemDetails)
				json.Unmarshal(bytes, &details)
				*detailsList = append(*detailsList, details)
				ch <- 0
			} else {
				cxt.Errorf("loadItemDetail unmarshal: %v", err)
				ch <- 0
			}
		} else {
			cxt.Errorf("loadItemDetail doing: %v", err)
			ch <- 0
		}
	} else {
		cxt.Errorf("loadItemDetail: %v", err)
		ch <- 0
	}
}

//Get object detail.
func getItemDetails(w http.ResponseWriter, r *http.Request, itemIds []int64) []*ItemDetails {
	detailsList := []*ItemDetails{}
	ch := make(chan int)

	for _, itemId := range itemIds {
		go loadItemDetail(w, r, itemId, &detailsList, ch)
	}

	c := len(itemIds)
	for i := 0; i < c; i++ {
		<-ch
	}

	return detailsList
}

//Dispatch messages to client, do details of push.
func dispatchOnClients(w http.ResponseWriter, r *http.Request, topicApi string, pItemDetailsList *[]*ItemDetails, pushedTime string) {
	//Dispatch details to client.
	if pItemDetailsList != nil {
		c := len(*pItemDetailsList)
		ch := make(chan int, c)
		for _, pItemDetail := range *pItemDetailsList {
			if pItemDetail != nil {
				go func(w http.ResponseWriter, r *http.Request, topicApi string, pItemDetail *ItemDetails, pushedTime string, ch chan int) {
					broadcast(w, r, topicApi, pItemDetail, pushedTime)
					ch <- 0
				}(w, r, topicApi, pItemDetail, pushedTime, ch)
			}
		}
		for i := 0; i < c; i++ {
			<-ch
		}
	}
	//Push summary to client.
	endCh := make(chan int)
	go summary(w, r, pItemDetailsList, pushedTime, endCh)
	<-endCh
}

//Push messages to clients.
func push(w http.ResponseWriter, r *http.Request, api string) {
	topicApi := (TOPICS + api)
	itemDetailsList := getItemDetails(w, r, getTopStories(w, r, api))
	t := time.Now()
	pushedTime := t.Format("20060102150405")

	dispatchCh := make(chan int)

	go func(w http.ResponseWriter,
		r *http.Request,
		topicApi string,
		pItemDetailsList *[]*ItemDetails,
		pushedTime string,
		dispatchCh chan int) {

		dispatchOnClients(w, r, topicApi, pItemDetailsList, pushedTime)
		dispatchCh <- 0
	}(w, r, topicApi, &itemDetailsList, pushedTime, dispatchCh)

	<-dispatchCh
}

//Send summary to clients.
func summary(w http.ResponseWriter, r *http.Request, pItemDetailsList *[]*ItemDetails, pushedTime string, endCh chan int) {
	//Push server can not accept to long contents.
	loopCount := len(*pItemDetailsList)
	if loopCount > SUMMARY_MAX {
		loopCount = SUMMARY_MAX
	}
	msgIds := ""
	summary := ""
	for i := 0; i < loopCount; i++ {
		summary += (((*pItemDetailsList)[i]).Title + "<tr>")
		msgIds += (strconv.FormatInt(((*pItemDetailsList)[i]).Id, 10) + ",")
	}
	pushedMsg := fmt.Sprintf(`{"to" : "%s", "data" : {"isSummary" : true, "summary": "%s", "ids": "%s", "count": %d, "pushed_time" : "%s"}}`,
		TOPICS + GET_SUMMARY,
		summary,
		msgIds,
		loopCount,
		pushedTime)
	pushedMsgBytes := bytes.NewBufferString(pushedMsg)

	cxt := appengine.NewContext(r)
	if req, err := http.NewRequest("POST", PUSH_SENDER, pushedMsgBytes); err == nil {
		req.Header.Add("Authorization", PUSH_KEY)
		req.Header.Add("Content-Type", API_RESTYPE)
		req.Header.Add("X-AppEngine-Cron", "true")

		client := urlfetch.Client(cxt)
		res, _ := client.Do(req)
		if res != nil {
			defer res.Body.Close()
		}
		if err != nil {
			cxt.Errorf("Push summary doing: %v", err)
		}
	} else {
		cxt.Errorf("Push summary: %v", err)
	}
	if endCh != nil {
		endCh <- 0
	}
}

func broadcast(w http.ResponseWriter, r *http.Request, topics string, pDetails *ItemDetails, pushedTime string) {
	pushedMsg := fmt.Sprintf(
		`{"to" : "%s","data" : {"by": "%s", "c_id": %d, "score": %d, "comments_count": %d, "text": "%s", "time": %d, "title": "%s", "url": "%s", "pushed_time" : "%s"}}`,
		topics,
		pDetails.By,
		pDetails.Id,
		pDetails.Score,
		len(pDetails.Kids),
		"", //details.Text,
		pDetails.Time,
		pDetails.Title,
		pDetails.Url,
		pushedTime)
	pushedMsgBytes := bytes.NewBufferString(pushedMsg)

	cxt := appengine.NewContext(r)
	if req, err := http.NewRequest("POST", PUSH_SENDER, pushedMsgBytes); err == nil {
		req.Header.Add("Authorization", PUSH_KEY)
		req.Header.Add("Content-Type", API_RESTYPE)
		req.Header.Add("X-AppEngine-Cron", "true")

		client := urlfetch.Client(cxt)
		res, err := client.Do(req)
		if res != nil {
			defer res.Body.Close()
		}
		if err != nil {
			cxt.Errorf("Push broadcast doing: %v", err)
		}
	} else {
		cxt.Errorf("Push broadcast: %v", err)
	}
}

//Sync top-news to client.
func sync(w http.ResponseWriter, r *http.Request) {
	cxt := appengine.NewContext(r)
	itemDetailsList := getItemDetails(w, r, getTopStories(w, r, GET_TOP_STORIES))
	syncItems := []SyncItemDetails{}
	t := time.Now()
	pushedTime := t.Format("20060102150405")
	for _, itemDetail := range itemDetailsList {
		//Same logical like dispatch, but I don't use routine.
		itemDetail.Title = strings.Replace(itemDetail.Title, "\"", "'", -1)
		itemDetail.Title = strings.Replace(itemDetail.Title, "%", "％", -1)
		itemDetail.Title = strings.Replace(itemDetail.Title, "\\", ",", -1)
		itemDetail.Url, _ = url.QueryUnescape(	itemDetail.Url )
		syncItem := SyncItemDetails{
			By:          itemDetail.By,
			Id:          itemDetail.Id,
			Kids:        len(itemDetail.Kids),
			Score:       itemDetail.Score,
			Text:        "", //itemDetail.Text,
			Time:        itemDetail.Time,
			Title:       itemDetail.Title,
			Url:         itemDetail.Url,
			Pushed_Time: pushedTime}
		syncItems = append(syncItems, syncItem)
	}
	if len(syncItems) > 0 {
		if json, err := json.Marshal(SyncItemDetailsList{syncItems}); err == nil {
			result := fmt.Sprintf(`%s`,string(json))
			w.Header().Set("Content-Type", API_RESTYPE)
			fmt.Fprintf(w, result)
		} else {
			cxt.Errorf("sync marshal: %v", err)
		}
	}
}
