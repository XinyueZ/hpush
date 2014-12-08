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

func push(_w http.ResponseWriter, _r *http.Request, _clients []OtherClient, _itemDetailsList []*ItemDetails) {
  if _clients != nil {
    t := time.Now()
    pushedTime := t.Format("20060102150405")
    for _, client := range _clients {
        var roundTotal int = 0
        for _, itemDetail := range _itemDetailsList {
            if roundTotal < client.MsgCount {
                if client.FullText && len(strings.TrimSpace(itemDetail.Text)) == 0 {
                  fmt.Fprintf(_w, "<font color=blue>------|%s|</font><p>", itemDetail.Text)
                } else  if !client.AllowEmptyUrl && len(strings.TrimSpace(itemDetail.Url)) == 0   {
                  fmt.Fprintf(_w, "<font color=blue>++++++|%s|</font><p>", itemDetail.Url)
                } else {
                  msg := broadcast(_w, _r, client.PushID, itemDetail, pushedTime)
                  fmt.Fprintf(_w, "<font color=red>Details:</font>%s<p>", msg)
                  roundTotal++
                }
            }
        }
    }
  }
}



func  broadcast(_w http.ResponseWriter, _r *http.Request, clientIds string, details *ItemDetails, pushedTime string ) (pushedMsg string) {
  pushedMsg = fmt.Sprintf(
    `{"registration_ids" : ["%s"],"data" : {"by": "%s", "c_id": %d, "score": %d, "text": "%s", "time": %d, "title": "%s", "url": "%s", "pushed_time" : "%s"}}`,
    clientIds,
    details.By,
    details.Id,
    details.Score,
    details.Text,
    details.Time,
    details.Title,
    details.Url,
    pushedTime)
  pushedMsgBytes := bytes.NewBufferString(pushedMsg)

  if req, err := http.NewRequest("POST", PUSH_SENDER, pushedMsgBytes); err == nil {
      req.Header.Add("Authorization", PUSH_KEY)
      req.Header.Add("Content-Type", API_RESTYPE)
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
  return
}
