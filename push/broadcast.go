package pushmessager

import (

  "appengine"
  "appengine/urlfetch"

  //"html/template"
  "net/http"
  "fmt"
  "io/ioutil"
  "bytes"

  /*
  "strconv"
  "encoding/json"
  "encoding/base64"


  "strings"
  */
  )

type TopStoresRes struct {
    Collection []int64
}


func getTopStories(_w http.ResponseWriter, _r *http.Request) {
    if req, err := http.NewRequest(API_METHOD, API_GET_TOP_STORIES, nil); err == nil {
        cxt := appengine.NewContext(_r)
        httpClient := urlfetch.Client(cxt)
        _r, err := httpClient.Do(req)
        defer _r.Body.Close()
        if err == nil {
          bytes, err := ioutil.ReadAll(_r.Body)
          topStoresRes := make([]int64,0)
          json.Unmarshal(bytes, &topStoresRes)
          fmt.Printf("%#v", topStoresRes)
        }
    }
}
