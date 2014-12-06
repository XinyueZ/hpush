package pushmessager

import (
  "appengine"
  "appengine/datastore"

  "fmt"
  "html/template"
  "net/http"
  "strconv"
)

var indexTemplate = template.Must(template.New("index").Parse(pushForm))
var responseTemplate = template.Must(template.New("response").Parse(responseForm))

const (
  pushForm = `
          <html>
              <body>
                </p>
                <p>Project hpush push-messages.</p></p>
                <form action="/response" method="post">
                  <div><input type="submit" value="PUSH" /></div>
                </form>

                <form action="/dela" method="post">
                  <div><input type="submit" value="REMOVE ALL USERS"/></div>
                </form>

            </body>
          </html>
        `
  responseForm = `<html><body><form action="/" method="post">{{.}}</p><div><input type="submit" value="Repush"></div></form></body></html>`
)

//func main() {
//}

type OtherClient struct {
  PushID string
  FullText bool
  MsgCount int
}


func init() {
  http.HandleFunc("/", handleRoot)
  http.HandleFunc("/insert", handleInsert)
  http.HandleFunc("/del", handleDelete)
  http.HandleFunc("/response", handleResponse)
  http.HandleFunc("/dela",handleDeleteAllUsers)
  http.HandleFunc("/edit",handleEdit)
}
/*
func login(_w http.ResponseWriter, _r *http.Request) {
  c := appengine.NewContext(_r)
  u := user.Current(c)
  if u == nil {
    url, err := user.LoginURL(c, _r.URL.String())
    if err != nil {
      http.Error(_w, err.Error(), http.StatusInternalServerError)
      return
    }
    _w.Header().Set("Location", url)
    _w.WriteHeader(http.StatusFound)
  }
}
*/
func handleRoot(_w http.ResponseWriter, _r *http.Request) {
//	login(_w, _r)
    indexTemplate.Execute(_w, nil)
}

func loadClients(_r *http.Request) (clients []OtherClient) {
  cxt := appengine.NewContext(_r)
  q := datastore.NewQuery("OtherClient")
  clients = make([]OtherClient, 0)
  q.GetAll(cxt, &clients)
  return
}

func handleResponse(_w http.ResponseWriter, _r *http.Request) {
    defer func() {
      if err := recover(); err != nil {
        fmt.Fprintf(_w, "Some error happened might: Nobody to be pushed")
      }
    }()
    detailsList := getItemDetails(_w, _r, getTopStories(_w, _r))
    clients := loadClients(_r)
    push(_w, _r, clients, detailsList)
    responseTemplate.Execute(_w, fmt.Sprintf("Finished client push users:%d",  len(clients)))
}

func handleDeleteAllUsers(_w http.ResponseWriter, _r *http.Request) {
    cxt := appengine.NewContext(_r)
    q := datastore.NewQuery("OtherClient")
    clients := make([]OtherClient, 0)
    keys, _ := q.GetAll(cxt, &clients)
    datastore.DeleteMulti(cxt,keys)

    //prints all data after deleting
    keys, _ = q.GetAll(cxt, &clients)
    fmt.Fprintf(_w, "rest:%v", len(keys) )
}


func handleInsert(_w http.ResponseWriter, _r *http.Request) {
    cookies := _r.Cookies()
    isFullText, _ := strconv.ParseBool(cookies[1].Value)
    msgCount, _ := strconv.Atoi(cookies[2].Value)
    otherClient := &OtherClient{cookies[0].Value, isFullText, msgCount}
    cxt := appengine.NewContext(_r)
    datastore.Put(cxt, datastore.NewIncompleteKey(cxt, "OtherClient", nil), otherClient)
    fmt.Fprintf(_w, otherClient.PushID )
}



func handleDelete(_w http.ResponseWriter, _r *http.Request) {
    cxt := appengine.NewContext(_r)
    cookies := _r.Cookies()
    q := datastore.NewQuery("OtherClient").Filter("PushID =", cookies[0].Value)
    clients := make([]OtherClient, 0)
    keys, _:= q.GetAll(cxt, &clients)
    datastore.DeleteMulti(cxt, keys);
}

func handleEdit(_w http.ResponseWriter, _r *http.Request) {
    cxt := appengine.NewContext(_r)
    cookies := _r.Cookies()
    q := datastore.NewQuery("OtherClient").Filter("PushID =", cookies[0].Value)
    clients := make([]OtherClient, 0)
    keys, _:= q.GetAll(cxt, &clients)
    isFullText, _ := strconv.ParseBool(cookies[1].Value)
    msgCount, _ := strconv.Atoi(cookies[2].Value)
    otherClient := &OtherClient{cookies[0].Value, isFullText, msgCount }
    datastore.Put(cxt, keys[0], otherClient);
}
