package main

import (
	"crypto/tls"
	"io/ioutil"
	"net/http"
	"os"
	"regexp"
	"strings"
	"time"

	"fyne.io/fyne/v2"
	"fyne.io/fyne/v2/app"
	"fyne.io/fyne/v2/container"
	"fyne.io/fyne/v2/dialog"
	"fyne.io/fyne/v2/layout"
	"fyne.io/fyne/v2/widget"
	"github.com/flopp/go-findfont"
)

func init() {
	fontPaths := findfont.List()
	for _, path := range fontPaths {
		if strings.Contains(path, "simkai.ttf") {
			os.Setenv("FYNE_FONT", path)
		}
	}
}

var result = widget.NewMultiLineEntry()

func main() {
	myApp := app.New()
	//myApp.Settings().SetTheme(theme.LightTheme()) // 白色
	myWin := myApp.NewWindow(" 快速获取 Druid Sessions")

	i := widget.NewEntry()
	i.SetPlaceHolder("url / file")

	result.SetPlaceHolder(`两种获取方式：
本地路径禁止存在 http 关键字，因为会误以为是远程获取

1、本地获取：
(druid 需要登录的情况下，下载json文件)
直接输入本地的 websession.json 文件路径。

2、远程获取：
(未授权的情况下使用远程获取)
http://127.0.0.1/druid/websession.json
http://127.0.0.1/system/druid/websession.json
http://127.0.0.1/webpage/system/druid/websession.json
	`)

	b := widget.NewButton("点击获取", func() {
		url := i.Text

		if strings.Contains(url, "http") {
			GetSession(url)
		} else if url == "" {
			dialog.ShowConfirm("Error", "靓仔你没有输入URL或文件路径是获取不到 Sessions 的", func(b bool) {}, myWin)
		} else {
			GetFile(url)
		}
	})

	c1 := container.New(layout.NewGridWrapLayout(fyne.NewSize(610, 35)), i)
	c2 := container.New(layout.NewHBoxLayout(), c1, b)

	c3 := container.New(layout.NewGridWrapLayout(fyne.NewSize(700, 350)), result)

	c := container.New(layout.NewVBoxLayout(), c2, c3)
	myWin.SetContent(c)
	myWin.Resize(fyne.NewSize(700, 400)) // 窗口大小
	myWin.SetFixedSize(true)             // 固定大小

	myWin.ShowAndRun() // 显示窗口

	defer os.Unsetenv("FYNE_FONT")
}

func GetSession(url string) {
	t := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
	}

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		result.SetText("请求失败：" + err.Error())
		return
	}
	req.Header.Add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36")
	c := &http.Client{
		Transport: t,
		Timeout:   5 * time.Second,
	}
	r, err := c.Do(req)
	if err != nil {
		result.SetText("请求失败：" + err.Error())
		return
	}
	defer r.Body.Close()

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		result.SetText("内容失败：" + err.Error())
		return
	}
	if r.StatusCode == 200 {
		re(string(body))
	} else {
		result.SetText("获取失败")
	}

}

func GetFile(filename string) {
	f, err := ioutil.ReadFile(filename)
	if err != nil {
		result.SetText("打开文件失败：" + err.Error())
		return
	}
	re(string(f))
}

func re(str string) {
	re := regexp.MustCompile(`"SESSIONID":"(.*?)"`)
	json := re.FindAllStringSubmatch(string(str), -1)
	var session string
	for _, v := range json {
		session += v[1] + "\n"
		result.SetText(session)
	}
}
