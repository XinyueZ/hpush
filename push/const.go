package pushmessager


const (
  //command: /Users/czhao/google-cloud-sdk/platform/google_appengine/appcfg.py update push/
  //Release,live
  API_KEY="AIzaSyAIC_FMDkMAkj6xsaolwCkgAqUcfQ7Qg7I" //Dec 5, 2014, 3:24:00 AM
  //API_KEY="AIzaSyBqw5aLHEdVjRzPl3BtE33NEkdDAz-yfpU" //Jan 8, 2015, 6:31:00 AM
  //Dev,stage
  //API_KEY="AIzaSyCTBktsn__VC5VzD93ccj-Jps3FDZcV4f0"

  API_HOST = "https://hacker-news.firebaseio.com"
  API_VERSION = "/v0"
  GET_TOP_STORIES = "/topstories.json"
  GET_ITEM_DETAILS = "/item/%d.json"
  API_GET_TOP_STORIES = API_HOST + API_VERSION + GET_TOP_STORIES
  API_GET_ITEM_DETAILS = API_HOST + API_VERSION + GET_ITEM_DETAILS

  PUSH_SENDER = "https://android.googleapis.com/gcm/send"
  PUSH_KEY = "key=" + API_KEY
  API_METHOD = "GET"
  API_RESTYPE = "application/json"

  TINY = "http://tinyurl.com/api-create.php?url="

  SUMMARY_MAX = 3
  // DEF_FULL_TEXT = true
  // DEF_MSG_COUNT = 3
)
