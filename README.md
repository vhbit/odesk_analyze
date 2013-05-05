# odesk_analyze

A simple per-task time tracker for oDesk.com

It was written exclusively as a "getting-to-know-Clojure" project.

## Usage

You have to get API secret and key through oDesk site and
also provide a company name and username.

All settings are stored in ~/.odeskrc in EDN format.

On the first start the app will provide you with URL. You have to paste it in
browser and login into oDesk. After succesful authorization press return
to continue tasks gathering.

Currently tasks are differentiated by [task-name-prefix] in memos.

For example, if you've logged the following 3 memos:

    [issue#64] I'm trying to fix it
    [issue#64] I'm trying to fix it
    [issue#65] Cool, another feature request!

the app will report 20 mins for issue#64 and 10 minutes for issue#65

Date interval should be specified from command line. If ommited only
today tasks will be counted.

Dates are quite flexible like "today", "2 days ago", "Apr 15" and so on.
For more details please visit [this page](http://natty.joestelmach.com/doc.jsp)

For example if you need task reports since Apr 25 till today you can write:

    odesk_analyze "Apr 25" "today"

To avoid server overloading, the app caches all the temporary results
in ~/.odesk_stats. That could be changed using :cache-dir in config.


## Sample config

    {
      :secret "xxxxxxxxx",
      :api-key "xxxxxxxxxxxxxxxxxx",
      :username "vhbit",
      :company "HelloWorld",
      :cache-dir "~/.odesk_stats"
    }



## License

Copyright (C) 2013 vhbit

Distributed under the Eclipse Public License, the same as Clojure.
