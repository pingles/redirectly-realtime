require 'rubygems'
require 'json'
require 'beanstalk-client'

beanstalk = Beanstalk::Pool.new(['localhost:11300'])
beanstalk.use('clicks')

(1..100000).each do |i|
  beanstalk.put({"keyword" => "hello"}.to_json)
end