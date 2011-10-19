<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<title>Fedora CloudSync: Login</title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

<link rel="shortcut icon" href="static/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="static/style.css"/>
<link rel="stylesheet" type="text/css" href="static/jquery-ui-1.8.12.custom.css"/>

<script type="text/javascript" src="static/jquery-1.5.2.min.js"></script>
<script type="text/javascript" src="static/jquery-ui-1.8.12.custom.min.js"></script>
<script type="text/javascript" src="static/json2.js"></script>
<script type="text/javascript" src="static/cloudsync-client.js"></script>
<script type="text/javascript"><!--

var restBaseUrl = document.location.href + "/../api/rest/";
var service = new CloudSyncClient(restBaseUrl);

$(function() {
  $("#button-login").button();
  document.f.j_username.focus();

  service.getServiceInfo(function(data) {
    $("#versionInfo").html("CloudSync v" + data.service.version);
    if (data.service.initialized == false) {
      $("#dialog-initialize").dialog({
          autoOpen: false,
          modal: true,
          width: 'auto',
          show: 'fade',
          hide: 'fade'
      });
      $("#dialog-initialize").dialog("option", "buttons", {
        "Create Account": function() {
          var username = $("#Initialize-username").val();
          var pass1 = $("#Initialize-password1").val();
          var pass2 = $("#Initialize-password2").val();
          if (username != "" && pass1 != "" && pass1 == pass2) {
            var serviceData = { service: {
              "initialAdminUsername" : username,
              "initialAdminPassword" : pass1
            }};
            service.updateServiceInfo(serviceData,
              function() {
                $("#dialog-initialize").dialog("close");
                document.f.j_username.focus();
              },
              function(httpRequest, method, url, textStatus) {
                alert("Account Creation Failed: " + textStatus);
              }
            );
          } else if (username == "") {
              alert("Username cannot be blank!");
          } else if (pass1 == "") {
              alert("Password cannot be blank!");
          } else {
              alert("Passwords do not match!");
          }
        }
      });
      $("#dialog-initialize").dialog("open");
    }
  },
  function(httpRequest, method, url, textStatus, errorThrown) {
      alert("Error getting " + url + " : " + textStatus);
  });  
  
});

//--></script>

</head>

<body bgcolor="#333333">

<form name='f' action='j_spring_security_check' method='POST'>
<center>
<p></p>
<p></p>
<div id="login">
  <p></p>
  <table bgcolor="#ffffff" cellpadding="10">
    <tr><td align="middle"><img src="static/logo.png"/></td></tr>
    <tr><td align="middle">
      <table cellpadding="5" style="font-size: 16px;">
        <tr><td>Username</td><td><input type='text' name='j_username' value=''></td></tr>
        <tr><td>Password</td><td><input type='password' name='j_password'/></td></tr>
      </table>
    </td></tr>
    <tr><td align="middle">
      <button id="button-login" onclick="document.f.submit();">Login &gt;</button>
    </td></tr>
  </table>
  <p id="versionInfo" style="color: #666666">
    ..
  </p>
</div>
</center>
</form>

<div class="ui-helper-hidden" id="dialog-initialize" title="Welcome!">
  <table>
    <tr>
      <td colspan="2">
        Before anyone can login, you'll need<br/>
        to create an administrative account.
        <p/>
      </td>
    </tr>
    <tr>
      <td>Username</td>
      <td><input id="Initialize-username" type="text" value=""/></td>
    </tr>
    <tr>
      <td>Password</td>
      <td><input id="Initialize-password1" type="password"/></td>
    </tr>
    <tr>
      <td>Password (again)</td>
      <td><input id="Initialize-password2" type="password"/></td>
    </tr>
  </table>
</div>

</body>
</html>
