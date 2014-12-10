package pushmessager

import (

  "appengine"
  "appengine/urlfetch"

  "net/http"
  "fmt"
  "io/ioutil"
  "encoding/json"
  "bytes"
  "time"
  "strings"
)

type TopStoresRes struct {
    Collection []int64
}


type ItemDetails struct {
  By string `by`
  Id int64 `id`
  Kids []int64 `kids`
  Score int64 `score`
  Text string `text`
  Time int64 `time`
  Title string `title`
  Type string `type`
  Url string `url`
}


func getTopStories(_w http.ResponseWriter, _r *http.Request) []int64{
    if req, err := http.NewRequest(API_METHOD, API_GET_TOP_STORIES, nil); err == nil {
        cxt := appengine.NewContext(_r)
        httpClient := urlfetch.Client(cxt)
        _r, err := httpClient.Do(req)
        defer _r.Body.Close()
        if err == nil {
          if bytes, err := ioutil.ReadAll(_r.Body); err == nil {
            topStoresRes := make([]int64,0)
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

func getItemDetails(_w http.ResponseWriter, _r *http.Request, itemIds []int64) ([]*ItemDetails) {
  detailsList := []*ItemDetails{}
  for _, itemId := range itemIds {
    api := fmt.Sprintf(API_GET_ITEM_DETAILS, itemId)
    if req, err := http.NewRequest(API_METHOD, api, nil); err == nil {
      cxt := appengine.NewContext(_r)
      httpClient := urlfetch.Client(cxt)
      res, err := httpClient.Do(req)
      defer res.Body.Close()
      if err == nil {
        if bytes, err := ioutil.ReadAll(res.Body); err == nil {
            details := new(ItemDetails)
            json.Unmarshal(bytes, &details)
            detailsList = append(detailsList, details)
          } else {
            fmt.Fprintf(_w, "%s", "Error by json-Unmarshal(details).")
          }
        } else {
            fmt.Fprintf(_w, "%s", "Error by doing client(details).")
        }
      } else {
            fmt.Fprintf(_w, "%s", "Error by making new request(details).")
      }
  }
  return detailsList
}

/*
func showDetailsList(_w http.ResponseWriter, _r *http.Request) {
    itemIds :=  getTopStories(_w, _r)
    if itemIds != nil {
      detailsList, _ :=   getItemDetails(_w, _r, itemIds )
      for _, detail := range detailsList {
        fmt.Fprintf(_w, "Details:%#v", *detail)
      }
    }
}
*/

func dispatch(_w http.ResponseWriter, _r *http.Request, roundTotal *int, pushedDetailList *[]*ItemDetails, client OtherClient, itemDetail *ItemDetails, pushedTime string, scheduledTask bool, ch chan int) {
    if *roundTotal < client.MsgCount {
        if client.FullText && len(strings.TrimSpace(itemDetail.Text)) == 0 {
          ch <- 0
        } else  if !client.AllowEmptyUrl && len(strings.TrimSpace(itemDetail.Url)) == 0   {
          ch <- 0
        } else {
          (*roundTotal)++
          *pushedDetailList = append(*pushedDetailList, itemDetail)
          broadcast(_w, _r, client.PushID, itemDetail, pushedTime, scheduledTask)
          ch <- 0
        }
    } else {
      ch <- 0
    }
}

func push(_w http.ResponseWriter, _r *http.Request, _clients []OtherClient, _itemDetailsList []*ItemDetails, scheduledTask bool) {
  if _clients != nil {
    t := time.Now()
    pushedTime := t.Format("20060102150405")
    end := make(chan int)
    for _, client := range _clients {
        pushedDetailList := []*ItemDetails{}
        ch := make(chan int)
        var roundTotal int = 0
        for _, itemDetail := range _itemDetailsList {
            go dispatch(_w, _r, &roundTotal, &pushedDetailList, client, itemDetail, pushedTime, scheduledTask, ch)
        }
        c := len(_itemDetailsList)
        for  i := 0; i < c; i++    {
          <-ch
        }
        roundTotal = 0

        go summary(_w, _r, client.PushID, pushedDetailList, pushedTime, scheduledTask, end )
    }

    c := len(_clients)
    for  i := 0; i < c; i++    {
        <-end
    }
  }
}


func  summary(_w http.ResponseWriter, _r *http.Request, clientIds string, pushedDetailList []*ItemDetails, pushedTime string, scheduledTask bool, ch chan int )  {
    pushedCount := len(pushedDetailList)
    //Push server can not accept to long contents.
    loopCount := pushedCount
    if loopCount > 5 {
       loopCount = 5
    }
    summary := ""
    for  i := 0; i < loopCount; i++    {
      summary += ( pushedDetailList[i].Title + "<tr>" )
    }

    pushedMsg := fmt.Sprintf(  `{"registration_ids" : ["%s"],"data" : {"isSummary" : true, "summary": "%s", "count": %d, "pushed_time" : "%s"}}`,
    clientIds,
    summary,
    pushedCount,
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
      defer res.Body.Close()
      if  err != nil {
        fmt.Fprintf(_w, "%s", "Error by doing client(broadcast).")
      }
    } else {
        fmt.Fprintf(_w, "%s", "Error by making new request(broadcast).")
    }
    //fmt.Fprintf(_w, "summary %s", pushedMsg)
    ch <- 0
}


func  broadcast(_w http.ResponseWriter, _r *http.Request, clientIds string, details *ItemDetails, pushedTime string , scheduledTask bool)  {
  pushedMsg := fmt.Sprintf(
    `{"registration_ids" : ["%s"],"data" : {"by": "%s", "c_id": %d, "score": %d, "comments_count": %d, "text": "%s", "time": %d, "title": "%s", "url": "%s", "pushed_time" : "%s"}}`,
    clientIds,
    details.By,
    details.Id,
    details.Score,
    len(details.Kids),
    "",//details.Text,
    details.Time,
    details.Title,
    details.Url,
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
      defer res.Body.Close()
      if  err != nil {
        fmt.Fprintf(_w, "%s", "Error by doing client(broadcast).")
      }
  } else {
      fmt.Fprintf(_w, "%s", "Error by making new request(broadcast).")
  }
}
