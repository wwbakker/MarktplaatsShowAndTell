#Credit limit tracker
## Description
This application is a proof of concept for a system that 
can keep track of credit limits from several sources.

## Requirements
- JDK 8
- SBT 1.2.3 (older versions might work, but this is untested)

## How to run?
- Clone this repository, and go to the cloned repository using your favorite shell. 
- Run `sbt run`.
- Go to [http://localhost:9000](http://localhost:9000)
```
PS E:\dev\repos\BETestWesselBakker> sbt run
Java HotSpot(TM) 64-Bit Server VM warning: ignoring option MaxPermSize=256m; support was removed in 8.0
[info] Loading settings for project global-plugins from idea.sbt ...
[info] Loading global plugins from C:\Users\Wessel\.sbt\1.0\plugins
[info] Loading settings for project betestwesselbakker-build from plugins.sbt,scaffold.sbt ...
[info] Loading project definition from E:\Dev\Repos\BETestWesselBakker\project
[info] Loading settings for project root from build.sbt ...
[info] Set current project to marktplaats-show-and-tell (in build file:/E:/Dev/Repos/BETestWesselBakker/)

--- (Running the application, auto-reloading is enabled) ---

[info] p.c.s.AkkaHttpServer - Listening for HTTP on /0:0:0:0:0:0:0:0:9000

(Server started, use Enter to stop and go back to the console...)
```


## Notes
There have been 2 changes to the Workbook files:
- `Workbook2.csv`: A dot has been added so that the contents (but not the format) are the same as in `Workbook2.prn` file. 
- `Workbook2.prn`: The "Credit Limit" column header has been replaced with "Credit-Limit", so that the auto detection for column name works. The application can do without it, but then the column lengths would have to be hard-coded. I thought this way it's more interesting. 
