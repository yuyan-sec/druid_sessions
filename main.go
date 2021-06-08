package main

import (
	"crypto/tls"
	"flag"
	"fmt"
	"io/ioutil"
	"net/http"
	"regexp"
	"time"
)

func get(url string) {
	t := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
	}

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return
	}
	req.Header.Add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36")
	c := &http.Client{
		Transport: t,
		Timeout:   5 * time.Second,
	}
	r, err := c.Do(req)
	if err != nil {
		return
	}
	defer r.Body.Close()

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		return
	}
	if r.StatusCode == 200 {
		re := regexp.MustCompile(`"SESSIONID":"(.*?)"`)
		json := re.FindAllStringSubmatch(string(body), -1)
		var result string
		for _, v := range json {
			result += v[1] + "\n"
			err := ioutil.WriteFile("./sessions.txt", []byte(result), 0644)
			if err != nil {
				fmt.Println(err)
				return
			}
		}
		fmt.Println("success")
	} else {
		fmt.Println("fail")
	}

}

func file(filename string) {
	f, err := ioutil.ReadFile(filename)
	if err != nil {
		fmt.Println(err)
		return
	}
	re := regexp.MustCompile(`"SESSIONID":"(.*?)"`)
	json := re.FindAllStringSubmatch(string(f), -1)
	var result string
	for _, v := range json {
		result += v[1] + "\n"
		err := ioutil.WriteFile("./sessions.txt", []byte(result), 0644)
		if err != nil {
			fmt.Println(err)
			return
		}
	}
	fmt.Println("success")
}

func main() {
	var url, filename string
	flag.StringVar(&url, "u", "", `远程文件地址：
http://127.0.0.1/druid/websession.json
http://127.0.0.1/system/druid/websession.json
http://127.0.0.1/webpage/system/druid/websession.json`)
	flag.StringVar(&filename, "f", "", "本地文件地址：./test.json")
	flag.Parse()
	if url != "" {
		get(url)
	} else if filename != "" {
		file(filename)
	} else {
		fmt.Println("你想得到session吗，想得美！")
	}
}
