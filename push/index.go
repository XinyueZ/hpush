package pushmessager

import (
	"appengine"

	"fmt"
	"net/http"
)

func init() {
	http.HandleFunc("/topstories", handleTopstories)
	http.HandleFunc("/newstories", handleNewstories)
	http.HandleFunc("/askstories", handleAskstories)
	http.HandleFunc("/showstories", handleShowstories)
	http.HandleFunc("/jobstories", handleJobstories)
}

func handleTopstories(w http.ResponseWriter, r *http.Request) {
	handle(w, r, GET_TOP_STORIES)
}

func handleNewstories(w http.ResponseWriter, r *http.Request) {
	handle(w, r, GET_NEWS_STORIES)
}

func handleAskstories(w http.ResponseWriter, r *http.Request) {
	handle(w, r, GET_ASK_STORIES)
}

func handleShowstories(w http.ResponseWriter, r *http.Request) {
	handle(w, r, GET_SHOW_STORIES)
}

func handleJobstories(w http.ResponseWriter, r *http.Request) {
	handle(w, r, GET_JOB_STORIES)
}

//Handle different request on different APIs.
func handle(w http.ResponseWriter, r *http.Request, api string) {
	defer func() {
		if err := recover(); err != nil {
			cxt := appengine.NewContext(r)
			cxt.Errorf("handle: %v", err)
		}
	}()
	push(w, r, api)
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//For sync request.
////////////////////////////////////////////////////////////////////////////////////////////////////
func handleSync(w http.ResponseWriter, r *http.Request) {
	sync(w, r)
}

////////////////////////////////////////////////////////////////////////////////////////////////////
func status(w http.ResponseWriter, ok bool, funcName string) {
	status := "false"
	if ok {
		status = "true"
	}
	s := fmt.Sprintf(`{"status":%s, "function":"%s" }`, status, funcName)
	w.Header().Set("Content-Type", API_RESTYPE)
	fmt.Fprintf(w, s)
}
