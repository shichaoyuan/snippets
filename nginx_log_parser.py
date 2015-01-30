import re

#log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
#                      '$status $body_bytes_sent "$http_referer" '
#                      '"$http_user_agent" "$http_x_forwarded_for"'
#                      ' $http_x_ab_network_type'
#                      ' $request_time $upstream_response_time $request_length $bytes_sent';


parts = [
    r'(?P<remote_addr>\S+)',
    r'\S+',
    r'(?P<remote_user>\S+)',
    r'\[(?P<time_local>.+)\]',
    r'"(?P<request>.+)"',
    r'(?P<status>[0-9]+)',
    r'(?P<body_bytes_sent>\S+)',
    r'"(?P<http_referer>.*)"',
    r'"(?P<http_user_agent>.*)"',
    r'"(?P<http_x_forwarded_for>.*)"',
    r'(?P<http_x_ab_network_type>\S+)',
    r'(?P<request_time>\S+)',
    r'(?P<upstream_response_time>\S+)',
    r'(?P<request_length>\S+)',
    r'(?P<bytes_sent>\S+)',
	
]

pattern = re.compile(r'\s+'.join(parts)+r'\s*\Z')

def parse(line):
    m = pattern.match(line)
    if m:
        return m.groupdict()
    else:
        return None


if __name__ == '__main__':
    line = '223.104.3.182 - - [30/Jan/2015:10:15:44 +0800] "POST /hot/feed/list HTTP/1.1" 200 423 "-" "%E5%8F%8B%E6%8B%8D/58 CFNetwork/711.1.16 Darwin/14.0.0" "-" - 0.019 0.019 622 616'
    print parse(line)

