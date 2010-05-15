require 'rubygems'
require 'json'
require 'beanstalk-client'

beanstalk = Beanstalk::Pool.new(['localhost:11300'])
beanstalk.use('clicks')

click = {
  "region"=>"uk",
  "timestamp"=>"Sat May 15 11:58:39 +0000 2010",
  "env"=>{
    "HTTP_HOST"=>"go.sp-ask.com",
    "HTTP_USER_AGENT"=>"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.9.1.9) Gecko/20100315 Firefox/3.5.9 (.NET CLR 3.5.30729) WinTSI 04.04.2010",
    "HTTP_ACCEPT_LANGUAGE"=>"en-gb,en;q=0.5",
    "REMOTE_ADDR"=>"10.241.38.17",
    "PATH_INFO"=>"/uk/r5",
    "HTTP_REFERER"=>"http://www.google.co.uk/url?q=chalmers+opticians&url=/aclk%3Fsa%3DL%26ai%3DCFQ18VIzuS_PkKIaFmQfxleyuAqS6w4EBqqSl7wuo4fT5LBADKANQkvnI5fj_____AWC7zs-D4ArIAQGqBBNP0K9oyv4wvbWrAt5v7vWKA8FH%26num%3D3%26sig%3DAGiWqtwo6PNpCfTVtYHYugw9cfCZiE06YQ%26q%3Dhttp://go.sp-ask.com/uk/r5%253Fq%253Dchalmers%252Bopticians%2526siteid%253D41439050%257C10000661%257C41439050&rct=j&ei=VIzuS8rSJ8T68Abun-n9Cg&usg=AFQjCNGLcc8PZwxkTTDkR4gKGIQsTSEgDg",
    "REQUEST_URI"=>"/uk/r5?q=chalmers+opticians&siteid=41439050|10000661|41439050",
    "HTTP_X_FORWARDED_FOR"=>"86.0.3.244",
    "rack.request.query_hash"=>{
      "siteid"=>"41439050|10000661|41439050",
      "q"=>"chalmers opticians"
      },
    "REQUEST_METHOD"=>"GET",
    "QUERY_STRING"=>"q=chalmers+opticians&siteid=41439050|10000661|41439050", "rack.request.query_string"=>"q=chalmers+opticians&siteid=41439050|10000661|41439050"}, 
    "destination"=>"http://uk.ask.com/ar?l=dis&qsrc=999&siteid=10000661&o=10000661&q=chalmers+opticians&ifr=1",
    "version"=>1,
    "client"=>"ask",
    "strategy"=>"r5",
    "params"=>{
      "siteid"=>"41439050|10000661|41439050",
      "q"=>"chalmers opticians"
    }
  }

1500.times do
  beanstalk.put(click.to_json)
end
