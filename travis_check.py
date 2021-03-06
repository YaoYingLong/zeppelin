#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# This script checks build status of given pullrequest identified by author and commit hash.
#
# usage)
#   python travis_check.py [author] [commit hash] [check interval (optional)]
#
# example)
#   # full hash
#   python travis_check.py Leemoonsoo 1f2549a38f440ebfbfe2d32a041684e3e39b496c
#
#   # with short hash
#   python travis_check.py Leemoonsoo 1f2549a
#
#   # with custom check interval
#   python travis_check.py Leemoonsoo 1f2549a 5,60,60

import json
import re
import requests
import sys
import time
import urllib3

# disable SNIMissingWarning. see https://urllib3.readthedocs.io/en/latest/advanced-usage.html#ssl-warnings
urllib3.disable_warnings()

author = sys.argv[1]
commit = sys.argv[2]

# check interval in sec
check = [5, 60, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 600, 600, 600, 600, 600, 600]

if len(sys.argv) > 3:
    check = map(lambda x: int(x), sys.argv[3].split(","))


def info(msg):
    print("[" + time.strftime("%Y-%m-%d %H:%M:%S") + "] " + msg)
    sys.stdout.flush()


info("Author: " + author + ", commit: " + commit)


def getBuildStatus(author, commit):
    travisApi = "https://api.travis-ci.org/"

    # get latest 25 builds
    resp = requests.get(url=travisApi + "/repos/" + author + "/zeppelin/builds")
    data = json.loads(resp.text)
    build = None

    if len(data) == 0:
        return build

    for b in data:
        if b["commit"][:len(commit)] == commit:
            resp = requests.get(url=travisApi + "/repos/" + author + "/zeppelin/builds/" + str(b["id"]))
            build = json.loads(resp.text)
            break

    return build


def status(index, msg, jobId):
    return '{:20}'.format(
        "[" + str(index + 1) + "] " + msg) + "https://travis-ci.org/" + author + "/zeppelin/jobs/" + str(jobId)


# load full build log and summarize
def logSummary(url):
    # test start pattern "Running org.apache.zeppelin.scheduler.ParallelSchedulerTest"
    testStartPattern = re.compile("^Running[ ](.*)")
    # test end pattern "Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.554 sec - in org.apache.zeppelin.scheduler.JobTest"
    testEndPattern = re.compile(
        "^Tests [^0-9]*([0-9]+)[^0-9]*([0-9]+)[^0-9]*([0-9]+)[^0-9]*([0-9]+)[^-]*[-][ ]in[ ](.*)")

    tests = {}
    resp = requests.get(url=url)
    lines = resp.text.splitlines()
    lastNonEmptyLine = ""
    indent = '{:10}'.format("")

    for line in lines:
        if not len(line.strip()) == 0:
            lastNonEmptyLine = line

        mStart = testStartPattern.match(line)
        if mStart:
            testName = mStart.group(1)
            tests[testName] = {
                "start": mStart
            }
            continue

        mEnd = testEndPattern.match(line)
        if mEnd:
            testName = mEnd.group(5)
            tests[testName]["end"] = mEnd
            continue

    for testName, test in tests.items():
        if not "end" in test:
            print(indent + "Test " + testName + " never finished")
        else:
            failures = int(test["end"].group(2))
            errors = int(test["end"].group(3))
            if failures > 0 or errors > 0:
                print(indent + test["end"].group(0))

    if not lastNonEmptyLine.startswith("Done"):
        print(indent + lastNonEmptyLine)
    print(indent + "Please check full log at " + url)


def printBuildStatus(build):
    failure = 0
    running = 0

    for index, job in enumerate(build["matrix"]):
        result = job["result"]
        jobId = job["id"]

        if job["started_at"] == None and result == None:
            print(status(index, "Not started", jobId))
            running = running + 1
        elif job["started_at"] != None and job["finished_at"] == None:
            print(status(index, "Running ...", jobId))
            running = running + 1
        elif job["started_at"] != None and job["finished_at"] != None:
            if result == None:
                print(status(index, "Not completed", jobId))
                failure = failure + 1
                logSummary("https://api.travis-ci.org/v3/job/" + str(jobId) + "/log.txt")
            elif result == 0:
                print(status(index, "OK", jobId))
            else:
                print(status(index, "Error " + str(result), jobId))
                failure = failure + 1
                logSummary("https://api.travis-ci.org/v3/job/" + str(jobId) + "/log.txt")
        else:
            print(status(index, "Unknown state", jobId))
            failure = failure + 1

    return failure, running


for sleep in check:
    info("--------------------------------")
    time.sleep(sleep)
    info("Get build status ...")
    build = getBuildStatus(author, commit)
    if build == None:
        info("Can't find build for commit " + commit + " from " + author)
        sys.exit(2)

    print("Build https://travis-ci.org/" + author + "/zeppelin/builds/" + str(build["id"]))
    failure, running = printBuildStatus(build)

    print(str(failure) + " job(s) failed, " + str(running) + " job(s) running/pending")

    if failure != 0:
        sys.exit(1)

    if failure == 0 and running == 0:
        info("CI Green!")
        sys.exit(0)

info("Timeout")
sys.exit(1)
