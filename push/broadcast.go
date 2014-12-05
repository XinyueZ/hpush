package pushmessager

import (

  "appengine"
  "appengine/urlfetch"

  //"html/template"
  "net/http"
  "fmt"
  "io/ioutil"
  "encoding/json"

  //"bytes"

  /*
  "strconv"

  "encoding/base64"


  "strings"
  */
  )

type TopStoresRes struct {
    Collection []int64
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
            fmt.Fprintf(_w, "Top-stories:%#v", topStoresRes)
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

type ItemDetails struct {
  By string
  Id int64
  Kids []int64
  Score int32
  Text string
  Time int64
  Title string
  Type string
  Url string
}

func getItemDetails(_w http.ResponseWriter, _r *http.Request, itemIds []int64) {
  for _, itemId := range itemIds {
    api := fmt.Sprintf(API_GET_ITEM_DETAILS, itemId)
    if req, err := http.NewRequest(API_METHOD, api, nil); err == nil {
      cxt := appengine.NewContext(_r)
      httpClient := urlfetch.Client(cxt)
      _r, err := httpClient.Do(req)
      defer _r.Body.Close()
      if err == nil {
        if bytes, err := ioutil.ReadAll(_r.Body); err == nil {
          details := new(ItemDetails)
          json.Unmarshal(bytes, &details)
          fmt.Fprintf(_w, "Item:%#v", details)
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
}

func pushNews(_w http.ResponseWriter, _r *http.Request) {
    itemIds :=  getTopStories(_w, _r)
    if itemIds != nil {
      getItemDetails(_w, _r, itemIds )
    }
}
