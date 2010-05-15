var sys = require("sys"),
  ws = require("./ws"), // WebSocket 
  port = 8080;

var bt = require('./beanstalk_client');

var connectedClients = [];

var sendToClients = function(data) {
  for (var i=0; i < connectedClients.length; i++) {
    connectedClients[i].write(data)
  };
}

bt.Client.connect('127.0.0.1:11300').addCallback(function(conn) {
  conn.watch('interesting');
  
  var messageHandler = function(job_id, job_json) {
    var parsed = JSON.parse(job_json);
    
    sendToClients(job_json)
    sys.puts('got job: ' + job_id + ' keyword: ' + parsed.keyword);
    
    conn.destroy(job_id);
    conn.reserve().addCallback(messageHandler);
  };

  conn.reserve().addCallback(messageHandler);
});

/** 
 * WebSocket server init
 */
var socket = (function(ws) {
    var srv;
    // Callbacks init
    srv = ws.createServer(function(websocket) {
      websocket.addListener("connect", function (resource) { 
          sys.puts("WebSocket connected " + resource);
          connectedClients.push(websocket);
      }).addListener("data", function (data) { 
      }).addListener("close", function () { 
          sys.puts("WebSocket closed");
          connectedClients = connectedClients.splice(connectedClients.indexOf(websocket), 1);
      });
    });

    return srv;
})(ws);

socket.listen(port);