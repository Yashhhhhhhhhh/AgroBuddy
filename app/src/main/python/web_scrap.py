import requests
from bs4 import BeautifulSoup
from urllib3.exceptions import InsecureRequestWarning
import warnings

warnings.simplefilter('ignore', InsecureRequestWarning)

# First URL
url1 = "https://pmkisan.gov.in/"

def fetch_and_parse(url1):
    r = requests.get(url1)
    html_doc = r.text
    soup = BeautifulSoup(html_doc, "html.parser")
    return soup

def get_links1():
    soup = fetch_and_parse(url1)
    links = soup.find_all("div", class_="col-12 col-sm-5 col-md-5")

    final_links1 = []

    for div in links:
        for idx, link in enumerate(div.find_all("li")):
            href = link.find("a")['href'].lstrip('/').strip()

            # Add base URL to the first two links
            if idx < 2:
                href = url1 + href

            # Replace spaces with %20
            href = href.replace(' ', '%20')
            final_links1.append(href)

    return final_links1

def get_info1():
    soup = fetch_and_parse(url1)
    info = []

    # Find the div with class "usefullinksarea"
    usefullinks_div = soup.find("div", class_="usefullinksarea")

    # Check if the div is found
    if usefullinks_div:
        # Find the ul with class "commonul" within the usefullinks_div
        ul_tag = usefullinks_div.find("ul", class_="commonul")

        # Check if the ul is found
        if ul_tag:
            # Find all <u> tags within the ul_tag
            u_tags = ul_tag.find_all("u")

            for u_tag in u_tags:
                text = u_tag.text.strip()
                info.append(text)

    return info

# Second URL
url2 = "https://www.agritech.tnau.ac.in/govt_schemes_services/govt_serv_schems_other_schemes_services.html"
url2_short="https://shorturl.at/CLRW1"
def fetch_and_parse_with_ssl_verification(url2):
    r = requests.get(url2, verify=False)
    html_doc = r.text
    soup = BeautifulSoup(html_doc, "html.parser")
    return soup

def get_links2():
    soup = fetch_and_parse_with_ssl_verification(url2)
    links = soup.find_all("tr")

    final_links = []

    for tr_tag in links[7:-1]:
        a_tag = tr_tag.find("a", href=True)
        if a_tag:
            href = a_tag['href'].lstrip('/').strip()
            final_links.append("https://www.agritech.tnau.ac.in/govt_schemes_services/" + href)

    return final_links




def get_info2():
    soup = fetch_and_parse_with_ssl_verification(url2)
    links = soup.find_all("tr")
    final_text = []
    for tr_tag in links[7:-1]:
        a_tag = tr_tag.find("a", href=True)
        if a_tag:
            text = a_tag.get_text().strip()
            final_text.append(text)
    return final_text


url3 = "https://agriwelfare.gov.in/en/Major"
url3_short ="https://agriwelfare.gov.in/"
def fetch_and_parse_url3(url3):
    r = requests.get(url3, verify=False)
    html_doc = r.text
    soup = BeautifulSoup(html_doc, "html.parser")
    return soup

def get_info3():
    soup = fetch_and_parse_url3(url3)
    table = soup.find("table", class_="table")
    rows = table.find_all("tr")[1:]  # Exclude the header row

    titles = []
    for row in rows:
        cells = row.find_all("td")
        title = cells[1].get_text(strip=True)
        titles.append(title)

    return titles
def get_links3():
    soup = fetch_and_parse_url3(url3)
    links = soup.select("table.table a[href]")
    final_links = []
    for link in links:
        href = link['href']
        # Check if the href contains "/Documents/"
        if "/Documents/" not in href:
            final_links.append(href)
    return final_links

def get_url1():
    return url1

def get_url2():
    return url2_short

def get_url3():
    return url3_short