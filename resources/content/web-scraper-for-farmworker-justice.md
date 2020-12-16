{
:title "Web Scraper for the Boycott Wendy's Campaign"
:pitch "Scraping for a good cause."
:location :coding-journal
:date "2019-02-28"
:repo "boycott-wendys-web-scraper"
:tags ["python" "selenium"]
}

Washtenaw Solidarity with Farmworkers, a grassroots [activist collective](https://www.facebook.com/washsolidaritywfarmworkers/) in Ann Arbor, advocates for American farmworkers in their struggle for human rights. In 2018 their focus was set on the fast food giant Wendy's presence on the University of Michigan's campus because of the corporation's refusal to join the [Fair Food Program](https://www.fairfoodprogram.org). WSF promoted the [ongoing boycott](https://www.boycott-wendys.org) of the restaurant and fought to convince the university to boycott Wendy's on-campus location by nixing their soon-to-be-expiring contract. They needed crucial support from the university's student groups, but since there are [thousands of them](https://maizepages.umich.edu/organizations), they were forced to endlessly copy and paste email addresses until their wrists ached.

When I joined Washtenaw Solidarity with Farmworkers, they had just started drafting their [petition](https://docs.google.com/forms/d/e/1FAIpQLSfdaenm6AwR6zV9haAVpCUXVaZD3AmsI-bCuii6IfBSkibY9w/viewform). To help with their efforts, I created `python` scripts to collect contact information for all of the student organizations at the University of Michigan, Eastern Michigan University, and Washtenaw Community College.

## Code Snippets

Let's focus on the University of Michigan script. First, we will import everything needed and setup a csv DictWriter to output my results to a file. Don't forgot to install `selenium` and `get_souped` using the package manager of your choice.
{{< highlight python >}}
from selenium import webdriver
import time
import re
from pathlib import Path
import csv
import json
from requestHandler import get_souped

# delete csv file if it exists
filename = "umich-all-org-emails.csv"
fileObj = Path(filename)
if fileObj.exists():
    fileObj.unlink()
    fileObj.touch()
csvFile = open(filename, 'w', newline='')

# prepare csv writer
writer = csv.DictWriter(csvFile, ["org_name", "first_name", "preferred_first_name", "last_name", "email"])
writer.writeheader()
{{< /highlight >}}

Now we're ready to look at the university's student organization page. A quick DevTools inspections reveals that it's a client-rendered React app. In order to load all the student organization links we need to let the page render client side and then click on the "Load More" button hundreds of times.

![maizepages](/img/maizepages.png)

Luckily, programatically interacting with web pages is possible with the [Selenium](https://docs.seleniumhq.org) python library. With Selenium we will create an instance of Firefox, visit the page shown above, and click the "load more" button until every organization link is rendered.

{{< highlight python >}}
# set up selenium driver
driver = webdriver.Firefox(firefox_profile=firefox_profile)
driver.implicitly_wait(0.5) # seconds
driver.get("https://maizepages.umich.edu/organizations")

# get the "load more" button from page
buttonDiv = driver.find_element_by_css_selector("#org-search-results + div")
loadMoreButton = buttonDiv.find_element_by_css_selector("button")

# click the "load more" button until it can't be clicked anymore
for x in range(0, 1000):
    try:
        loadMoreButton.click()
        time.sleep(.1)
    except:
        break
{{< /highlight >}}

Now that all the links are loaded, we'll gather all the links via css selectors.

{{< highlight python >}}
# get all the urls to the individual org pages
orgSearchResults = driver.find_element_by_id("org-search-results")
linkTags = orgSearchResults.find_elements_by_css_selector("a")
orgPageUrls = [link.get_attribute("href") for link in linkTags]
{{< /highlight >}}

Finally, we will visit every student organization page and collect the relevant contact information, writing each record to a csv file on disk.

![maizepages](/img/maizepages2.png)

Another quick DevTools inspection shows us this page actually contains all the contact info before it is rendered client side. The data is stored in a script tag in the form of a javascript object. That's great! We don't even have to use Selenium since a simple GET request and response will suffice.

![maizepages](/img/maizepages3.png)

Let's use the [Beautiful Soup](https://www.crummy.com/software/BeautifulSoup/) library to send fetch the page HTML, regex to locate the javascipt code, the built-in `json` library to parse the code, and the built-in `csv` library to write the data to disk.

{{< highlight python >}}
#regex patterns for the html response
orgNamePattern = re.compile('(?:"name":")(.+)(?:","short)')
primaryContactPattern = re.compile('(?:"primaryContact":)(.+?)(?:,"isBranch")')

# loop through the urls
for orgPageUrl in orgPageUrls:
    print("orgPageUrl: " + orgPageUrl)
    #get a beautiful soup object with url response loaded
    html = get_souped(orgPageUrl).text
    #search the html for the org name
    orgNameSearch = orgNamePattern.search(html)
    if orgNameSearch is None:
        print("ERROR: name not found at " + orgPageUrl)
        break
    else:
        orgName = orgNameSearch.group(1)
    #search the html for the primary contact json object
    primaryContactSearch = primaryContactPattern.search(html)
    if primaryContactSearch is None:
        print("ERROR: primaryContactInfo not found at " + orgPageUrl)
        break
    else:
        # load it as a python dict (nulls get turned into Nones)
        primaryContactDict = json.loads(primaryContactSearch.group(1))

    #if there is no primaryContact json object then skip this org
    if primaryContactDict is None:
        continue

    # construct dict to write to csv file
    finalDict = {
        "org_name": orgName,
        "first_name": primaryContactDict["firstName"],
        "preferred_first_name": primaryContactDict["preferredFirstName"],
        "last_name": primaryContactDict["lastName"],
        "email": primaryContactDict["primaryEmailAddress"]
    }
    writer.writerow(finalDict)
    print(finalDict)

# close system resources
csvFile.close()
driver.close()
{{< /highlight >}}

With these email addresses, Washtenaw Solidarity with Farmworkers collected hundreds of signatures for a petition presented to the university's Board of Regents and Board of Trustees. Ultimately, the Wendy's on campus [decided not to renew their contract](https://www.metrotimes.com/table-and-bar/archives/2019/02/18/university-of-michigan-cuts-ties-with-wendys-restaurant) due to the political pressure brought forth by the boycott. Washtenaw Solidarity with Farmworkers also persuaded the Ann Arbor City Council to [urge Ann Arbor residents](https://www.wxyz.com/ann-arbor-city-council-urges-boycott-of-wendys-hamburgers-over-fair-food-program) to support the boycott as well. Dr. Kim Daley, the leader of Washtenaw Solidarity with Farmworkers, gave an excellent interview about all these events on local television.

{{< yt CBHuGUtL2SQ >}}
