#### 如何添加图集
首先需要使用 [pyspider](https://github.com/binux/pyspider) 抓取图片网站上的图片信息, 然后将抓取的结果的 [**URL-JSON**](http://23.252.109.110:5000/results/dump/meizitu.txt) 网址添加到 App, 使用pyspider创建多个项目时保证项目名称唯一, App 添加页面输入名称时不能和 App 中现有的图集名称冲突

#### 如何使用 pyspider
请参考 pyspider 的[文档](
http://docs.pyspider.org/), 或者这篇[博客](http://blog.binux.me/2015/01/pyspider-tutorial-level-1-html-and-css-selector/)

#### 抓取网页哪些信息
 * title
 * url
 * date
 * imgs (url)
 * author (value 可为空)
 
可参看抓取[妹子图](http://www.meizitu.com)的 pyspider [脚本](https://github.com/scola/Meizitu/blob/master/pyspider/meizitu.py), 不能改变返回字典的 key 的名称