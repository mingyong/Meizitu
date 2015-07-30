#!/usr/bin/env python
# -*- encoding: utf-8 -*-
# Created on 2015-04-17 19:52:20
# Project: ganji_hedong

from pyspider.libs.base_handler import *
import re

class Handler(BaseHandler):
    crawl_config = {
    }

    @every(minutes=24 * 60)
    def on_start(self):
        self.crawl('http://www.douban.com/group/haixiuzu/discussion', callback=self.index_page)

    @config(age=12 * 60 * 60)
    def index_page(self, response):
        for each in response.doc('div.article>div>table.olt>tr[class=""]').items():
            self.crawl(each('td>a[href^="http"]').attr.href, callback=self.detail_page)
            
        # next page
        for each in response.doc('div.paginator > span.next > a').items():
            if int(each.attr.href.split('=')[-1]) <= 100:
                self.crawl(each.attr.href, callback=self.index_page)

    @config(priority=3)
    def detail_page(self, response):
        imgs = [x.attr.src for x in response.doc('div.topic-content > div.topic-figure > img').items()]
        if not imgs: return None
        author = response.doc('div.topic-content>div.topic-doc>h3>span.from>a')
        author_name = author.text()
        author_href = author.attr.href
        author_info = {author_name: author_href}
        return {
            "url": response.url,
            "title": response.doc('title').text(),
            "date": response.doc('div.topic-content>div.topic-doc>h3>span.color-green').text(),
            "author": author_info,
            "imgs": imgs,
        }
