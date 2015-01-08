package pushmessager

import (
	"appengine"
	"appengine/urlfetch"

	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"strings"
	"time"
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

func getTopStories(_w http.ResponseWriter, _r *http.Request) []int64 {
	if req, err := http.NewRequest(API_METHOD, API_GET_TOP_STORIES, nil); err == nil {
		cxt := appengine.NewContext(_r)
		httpClient := urlfetch.Client(cxt)
		_r, err := httpClient.Do(req)
		defer _r.Body.Close()
		if err == nil {
			if bytes, err := ioutil.ReadAll(_r.Body); err == nil {
				topStoresRes := make([]int64, 0)
				json.Unmarshal(bytes, &topStoresRes)
				//fmt.Fprintf(_w, "Top-stories:%#v", topStoresRes)
				return topStoresRes
			} else {
				fmt.Fprintf(_w, "%s", "Error by json-Unmarshal(top-stories).")
			}
		} else {
			fmt.Fprintf(_w, "%s", "Error by doing client(top-stories).")
		}
	} else {
		fmt.Fprintf(_w, "%s", "Error by making new request(top-stories).")
	}
	return nil
}

func loadDetailsList(_w http.ResponseWriter, _r *http.Request, itemId int64, detailsList *[]*ItemDetails, ch chan int) {
	api := fmt.Sprintf(API_GET_ITEM_DETAILS, itemId)
	if req, err := http.NewRequest(API_METHOD, api, nil); err == nil {
		cxt := appengine.NewContext(_r)
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
				ch <- 0
			}
		} else {
			ch <- 0
		}
	} else {
		ch <- 0
	}
}

func getItemDetails(_w http.ResponseWriter, _r *http.Request, itemIds []int64) []*ItemDetails {
	detailsList := []*ItemDetails{}
	ch := make(chan int)

	for _, itemId := range itemIds {
		go loadDetailsList(_w, _r, itemId, &detailsList, ch)
	}

	c := len(itemIds)
	for i := 0; i < c; i++ {
		<-ch
	}

	return detailsList
}

func dispatch(_w http.ResponseWriter, _r *http.Request, roundTotal *int,
	client OtherClient, itemDetail *ItemDetails, pushedTime string, scheduledTask bool, ch chan int) (brk bool) {
	brk = false
	if ch != nil {
		if *roundTotal < client.MsgCount {
			if client.FullText && len(strings.TrimSpace(itemDetail.Text)) == 0 {
				ch <- 0
			} else if !client.AllowEmptyUrl && len(strings.TrimSpace(itemDetail.Url)) == 0 {
				ch <- 0
			} else {
				(*roundTotal)++
				broadcast(_w, _r, client.PushID, itemDetail, pushedTime, scheduledTask)
				ch <- 0
			}
		} else {
			ch <- 0
		}
	} else {
		if *roundTotal < client.MsgCount {
			if client.FullText && len(strings.TrimSpace(itemDetail.Text)) == 0 {
			} else if !client.AllowEmptyUrl && len(strings.TrimSpace(itemDetail.Url)) == 0 {
			} else {
				(*roundTotal)++
				broadcast(_w, _r, client.PushID, itemDetail, pushedTime, scheduledTask)
			}
		} else {
			brk = true
		}
	}
	return
}

func dispatchOnClients(_w http.ResponseWriter, _r *http.Request, _itemDetailsList []*ItemDetails, client OtherClient, pushedTime string, scheduledTask bool, dispatchCh chan int, syncType bool) {
	var roundTotal int = 0
	if !syncType {
		ch := make(chan int)
		for _, itemDetail := range _itemDetailsList {
			go dispatch(_w, _r, &roundTotal, client, itemDetail, pushedTime, scheduledTask, ch)
		}
		c := len(_itemDetailsList)
		for i := 0; i < c; i++ {
			<-ch
		}
	} else {
		for _, itemDetail := range _itemDetailsList {
			brk := dispatch(_w, _r, &roundTotal, client, itemDetail, pushedTime, scheduledTask, nil)
			if brk {
				break
			}
		}
	}

	if !syncType {
		endCh := make(chan int)
		go summary(_w, _r, client, _itemDetailsList, pushedTime, scheduledTask, endCh)
		<-endCh
	} else {
		summary(_w, _r, client, _itemDetailsList, pushedTime, scheduledTask, nil)
	}
	dispatchCh <- 0
}

func push(_w http.ResponseWriter, _r *http.Request, _clients []OtherClient, scheduledTask bool) {
	if _clients != nil {
		_itemDetailsList := getItemDetails(_w, _r, getTopStories(_w, _r))
		t := time.Now()
		pushedTime := t.Format("20060102150405")

		//Check how many clients needs PUSH.
		totalDispatch := len(_clients)
		dispatchCh := make(chan int)

		//Dispatch PUSHs to clients.
		for _, client := range _clients {
			go dispatchOnClients(_w, _r, _itemDetailsList, client, pushedTime, scheduledTask, dispatchCh, true)
		}

		//Wait for all pushed clients.
		for i := 0; i < totalDispatch; i++ {
			<-dispatchCh
		}
	}
}

func summary(_w http.ResponseWriter, _r *http.Request, _client OtherClient, pushedDetailList []*ItemDetails, pushedTime string, scheduledTask bool, endCh chan int) {
	//Push server can not accept to long contents.
	loopCount := len(pushedDetailList)
	if loopCount > SUMMARY_MAX {
		loopCount = SUMMARY_MAX
	}
	summary := ""
	for i := 0; i < loopCount; i++ {
		summary += (pushedDetailList[i].Title + "<tr>")
	}
	pushedMsg := fmt.Sprintf(`{"registration_ids" : ["%s"],"data" : {"isSummary" : true, "summary": "%s", "count": %d, "pushed_time" : "%s"}}`,
		_client.PushID,
		summary,
		_client.MsgCount,
		pushedTime)
	pushedMsgBytes := bytes.NewBufferString(pushedMsg)

	if req, err := http.NewRequest("POST", PUSH_SENDER, pushedMsgBytes); err == nil {
		req.Header.Add("Authorization", PUSH_KEY)
		req.Header.Add("Content-Type", API_RESTYPE)
		if scheduledTask {
			req.Header.Add("X-AppEngine-Cron", "true")
		}
		c := appengine.NewContext(_r)
		client := urlfetch.Client(c)
		res, _ := client.Do(req)
		if res != nil {
			defer res.Body.Close()
		}
		if err != nil {
			c.Errorf("Push summary: %v", err)
		}
	}
	if endCh != nil {
		endCh <- 0
	}
}

func broadcast(_w http.ResponseWriter, _r *http.Request, clientIds string, details *ItemDetails, pushedTime string, scheduledTask bool) {
	pushedMsg := fmt.Sprintf(
		`{"registration_ids" : ["%s"],"data" : {"by": "%s", "c_id": %d, "score": %d, "comments_count": %d, "text": "%s", "time": %d, "title": "%s", "url": "%s", "pushed_time" : "%s"}}`,
		clientIds,
		details.By,
		details.Id,
		details.Score,
		len(details.Kids),
		"", //details.Text,
		details.Time,
		details.Title,
		getTinyUrl(_w, _r, details.Url),
		pushedTime)
	pushedMsgBytes := bytes.NewBufferString(pushedMsg)

	if req, err := http.NewRequest("POST", PUSH_SENDER, pushedMsgBytes); err == nil {
		req.Header.Add("Authorization", PUSH_KEY)
		req.Header.Add("Content-Type", API_RESTYPE)
		if scheduledTask {
			req.Header.Add("X-AppEngine-Cron", "true")
		}
		c := appengine.NewContext(_r)
		client := urlfetch.Client(c)
		res, err := client.Do(req)
		if res != nil {
			defer res.Body.Close()
		}
		if err != nil {
			c.Errorf("Push broadcast: %v", err)
		}
	}
}

func sync(_w http.ResponseWriter, _r *http.Request, client *OtherClient) {
	_itemDetailsList := getItemDetails(_w, _r, getTopStories(_w, _r))
	syncItems := []SyncItemDetails{}
	var roundTotal int = 0
	t := time.Now()
	pushedTime := t.Format("20060102150405")
	for _, itemDetail := range _itemDetailsList {
		//Same logical like dispatch, but I don't use routine.
		if roundTotal < client.MsgCount {
			if client.FullText && len(strings.TrimSpace(itemDetail.Text)) == 0 {
			} else if !client.AllowEmptyUrl && len(strings.TrimSpace(itemDetail.Url)) == 0 {
			} else {
				roundTotal++
				syncItem := SyncItemDetails{
					By:          itemDetail.By,
					Id:          itemDetail.Id,
					Kids:        len(itemDetail.Kids),
					Score:       itemDetail.Score,
					Text:        "", //itemDetail.Text,
					Time:        itemDetail.Time,
					Title:       itemDetail.Title,
					Url:         getTinyUrl(_w, _r, itemDetail.Url),
					Pushed_Time: pushedTime}
				syncItems = append(syncItems, syncItem)
			}
		} else {
			break
		}
	}
	json, _ := json.Marshal(SyncItemDetailsList{syncItems})
	_w.Header().Set("Content-Type", API_RESTYPE)
	fmt.Fprintf(_w, string(json))
}

func getTinyUrl(_w http.ResponseWriter, _r *http.Request, orignalUrl string) (tingUrl string) {
	if orignalUrl != "" {
		if req, err := http.NewRequest(API_METHOD, TINY+orignalUrl, nil); err == nil {
			cxt := appengine.NewContext(_r)
			httpClient := urlfetch.Client(cxt)
			res, err := httpClient.Do(req)
			if res != nil {
				defer res.Body.Close()
			}
			if err == nil {
				if bytes, err := ioutil.ReadAll(res.Body); err == nil {
					tingUrl = string(bytes)
				} else {
					tingUrl = orignalUrl
				}
			} else {
				tingUrl = orignalUrl
			}
		}
	}
	return
}
