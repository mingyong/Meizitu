#!/usr/bin/env python
# -*- encoding: utf-8 -*-
# Created on 2015-04-17 19:52:20
# Project: meizitu

from pyspider.libs.base_handler import *
import re

class Handler(BaseHandler):
    crawl_config = {
    }

    @every(minutes=24 * 60)
    def on_start(self):
        self.crawl('http://www.meizitu.com/', callback=self.index_page)

    @config(age=10 * 24 * 60 * 60)
    def index_page(self, response):
        topic_list = []
        average = 0
        for each in response.doc('html>body>div#wrapper>div#container>div#pagecontent>div#maincontent>div.postmeta>div.metaRight>h2>a').items():
            self.crawl(each.attr.href, callback=self.detail_page)
            
        # next page
        for each in response.doc('html>body>div#wrapper>div#container>div#pagecontent>div#maincontent>div.navigation>div#wp_page_numbers>ul>li>a').items():
            if each.text().isdigit() and int(each.text()) <= 5:
                self.crawl(each.attr.href, callback=self.index_page)

    @config(priority=3)
    def detail_page(self, response):
        imgs = [x.attr.src for x in response.doc('html>body>div#wrapper>div#container>div#pagecontent>div#maincontent>div.postContent>div#picture>p>img').items()]
        if not imgs: return None
        author_info = {}
        month_Year = response.doc('html>body>div#wrapper>div#container>div#pagecontent>div#maincontent>div.postmeta>div.metaLeft>div.month_Year').text()
        day = response.doc('html>body>div#wrapper>div#container>div#pagecontent>div#maincontent>div.postmeta>div.metaLeft>div.day').text()
        date = '-'.join(month_Year.split()[::-1] + day.split())
        return {
            "url": response.url,
            "title": response.doc('title').text().split(" ")[0],
            "date": date,
            "author": author_info,
            "imgs": imgs,
        }
