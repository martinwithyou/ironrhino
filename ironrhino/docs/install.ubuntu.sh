#!/bin/sh

#must run with sudo
if [ ! -n "$SUDO_USER" ];then
echo please run sudo $0
exit 1
else
USER="$SUDO_USER"
fi

#install packages
apt-get --force-yes --yes install openjdk-7-jdk ant mysql-server subversion nginx chkconfig sysv-rc-conf fontconfig xfonts-utils unzip iptables make gcc

#install simsun font
if [ -f "simsun.ttf" ]; then
mv simsun.ttf /usr/share/fonts/truetype
chmod 644 /usr/share/fonts/truetype/simsun.ttf
cd /usr/share/fonts
mkfontscale
mkfontdir
fc-cache -fv
fi

#install tomcat
if $(ls -l apache-tomcat-*.tar.gz >/dev/null 2>&1) && ! $(ls -l tomcat8080 >/dev/null 2>&1);then 
tar xvf apache-tomcat-*.tar.gz >/dev/null && rm -rf apache-tomcat-*.tar.gz
rename s/^apache-tomcat.*$/tomcat/g apache-tomcat-*
cd tomcat && rm -rf bin/*.bat && rm -rf webapps/* && cd ..
sed -i '99i CATALINA_OPTS="-server -Xms128m -Xmx1024m -Xmn80m -Xss256k -XX:PermSize=128m -XX:MaxPermSize=512m -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+UseParNewGC -XX:CMSMaxAbortablePrecleanTime=5"' tomcat/bin/catalina.sh
cp -R tomcat tomcat8080
cp -R tomcat tomcat8081
rm -rf tomcat
sed -i '99i CATALINA_PID="/tmp/tomcat8080_pid"' tomcat8080/bin/catalina.sh
sed -i '99i CATALINA_PID="/tmp/tomcat8081_pid"' tomcat8081/bin/catalina.sh
cat>tomcat8080/conf/server.xml<<EOF
<?xml version='1.0' encoding='utf-8'?>
<Server port="8005" shutdown="SHUTDOWN">
  <Service name="Catalina">
    <Connector port="8080" protocol="org.apache.coyote.http11.Http11NioProtocol" connectionTimeout="20000" redirectPort="8443" URIEncoding="UTF-8" useBodyEncodingForURI="true" enableLookups="false" bindOnInit="false" server="ironrhino" maxPostSize="4194304"/>
    <Engine name="Catalina" defaultHost="localhost">
      <Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="false">
      </Host>
    </Engine>
  </Service>
</Server>
EOF
cat>tomcat8081/conf/server.xml<<EOF
<?xml version='1.0' encoding='utf-8'?>
<Server port="8006" shutdown="SHUTDOWN">
  <Service name="Catalina">
    <Connector port="8081" protocol="org.apache.coyote.http11.Http11NioProtocol" connectionTimeout="20000" redirectPort="8443" URIEncoding="UTF-8" useBodyEncodingForURI="true" enableLookups="false" bindOnInit="false" server="ironrhino" maxPostSize="4194304"/>
    <Engine name="Catalina" defaultHost="localhost">
      <Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="false">
      </Host>
    </Engine>
  </Service>
</Server>
EOF
chown -R $USER:$USER tomcat*
fi

if [ ! -f /etc/init.d/tomcat8080 ]; then
cat>/etc/init.d/tomcat8080<<EOF
#!/bin/sh
#
# Startup script for the tomcat
#
# chkconfig: 345 80 15
# description: Tomcat
user=$USER

case "\$1" in
start)
       su \$user -c "/home/$USER/tomcat8080/bin/catalina.sh start"
       ;;
stop)
       su \$user -c "/home/$USER/tomcat8080/bin/catalina.sh stop -force"
       ;;
restart)
       su \$user -c "/home/$USER/tomcat8080/bin/catalina.sh stop -force"
       su \$user -c "/home/$USER/tomcat8080/bin/catalina.sh start"
       ;;
*)
       echo "Usage: \$0 {start|stop|restart}"
       esac

exit 0
EOF
chmod +x /etc/init.d/tomcat8080
update-rc.d tomcat8080 defaults
fi

if [ ! -f /etc/init.d/tomcat8081 ]; then
cat>/etc/init.d/tomcat8081<<EOF
#!/bin/sh
#
# Startup script for the tomcat
#
# chkconfig: 345 80 15
# description: Tomcat
user=$USER

case "\$1" in
start)
       su \$user -c "/home/$USER/tomcat8081/bin/catalina.sh start"
       ;;
stop)
       su \$user -c "/home/$USER/tomcat8081/bin/catalina.sh stop -force"
       ;;
restart)
       su \$user -c "/home/$USER/tomcat8081/bin/catalina.sh stop -force"
       su \$user -c "/home/$USER/tomcat8081/bin/catalina.sh start"
       ;;
*)
       echo "Usage: \$0 {start|stop|restart}"
       esac

exit 0
EOF
chmod +x /etc/init.d/tomcat8081
update-rc.d tomcat8081 defaults
fi


#config nginx
if [ ! -f /etc/nginx/sites-available/default ]; then
cat>/etc/nginx/sites-available/default<<EOF
gzip_min_length  1024;
gzip_types       text/xml text/css text/javascript application/x-javascript;
upstream  backend  {
    server   localhost:8080;
    server   localhost:8081;
}
server {
     listen   80 default_server;
     location ~ ^/assets/ {
             root   /home/$USER/tomcat8080/webapps/ROOT;
             expires      max;
             add_header Cache-Control public;
             charset utf-8;
     }
     location  / {
             proxy_pass  http://backend;
             proxy_redirect    off;
             proxy_set_header  X-Forwarded-For  \$proxy_add_x_forwarded_for;
             proxy_set_header  X-Real-IP  \$remote_addr;
             proxy_set_header  Host \$http_host;
     }
}
EOF
service nginx restart
fi


#generate deploy.sh
if [ ! -f deploy.sh ]; then
cat>deploy.sh<<EOF
if [ "\$1" = "" ];  then
    echo "please run \$0 name"
    exit 1
elif [ ! -d "\$1" ]; then
    echo "directory \$1 doesn't exists"
    exit 1
fi

cd ironrhino && svn up && ant dist && cd ..
cd \$1 && svn up
rm -rf webapp/WEB-INF/lib/*
ant -Dserver.home=/home/$USER/tomcat8080 -Dwebapp.deploy.dir=/home/$USER/tomcat8080/webapps/ROOT deploy

ant -Dserver.home=/home/$USER/tomcat8081 -Dserver.shutdown.port=8006 -Dserver.startup.port=8081 shutdown
rm -rf /home/$USER/tomcat8081/webapps
mkdir -p /home/$USER/tomcat8081/webapps/ROOT
cp -R /home/$USER/tomcat8080/webapps/ROOT /home/$USER/tomcat8081/webapps/ROOT
ant -Dserver.home=/home/$USER/tomcat8081 -Dserver.shutdown.port=8006 -Dserver.startup.port=8081 startup
EOF
chown $USER:$USER deploy.sh
fi

#generate backup.sh
if [ ! -f backup.sh ]; then
cat>backup.sh<<EOF
date=`date +%Y-%m-%d`
backupdir=/home/$USER/backup/\$date
if test ! -d \$backupdir
then  mkdir -p \$backupdir
fi
cp -r /var/lib/mysql/xiangling \$backupdir
cp -r /home/$USER/web/assets/upload \$backupdir
mysql -u root -D ironrhino -e "optimize table user;"
olddate=`date +%F -d"-30 days"`
rm -rf /home/$USER/backup/\$olddate*
chown -R $USER:$USER /home/$USER/backup
EOF
chown $USER:$USER backup.sh
fi


#iptables
if [ ! -f /etc/init.d/iptables ]; then
cat>/etc/init.d/iptables<<EOF
#!/bin/sh
#
# Startup script for the tomcat
#
# chkconfig: 345 80 15
# description: Tomcat
user=$USER

case "\$1" in
start)
	iptables -A INPUT -s 127.0.0.1 -d 127.0.0.1 -j ACCEPT
	iptables -A INPUT -p tcp --dport 8080 -j DROP
	iptables -A INPUT -p tcp --dport 8081 -j DROP
       ;;
stop)
	iptables -F
	iptables -X
	iptables -Z
       ;;
*)
       echo "Usage: \$0 {start|stop}"
       esac

exit 0
EOF
chmod +x /etc/init.d/iptables
update-rc.d iptables defaults
service iptables start
fi

#install redis
if $(ls -l redis-*.tar.gz >/dev/null 2>&1);then 
tar xvf redis-*.tar.gz >/dev/null && rm -rf redis-*.tar.gz
rename s/^redis.*$/redis/g redis-*
cd redis && make && make install
cd utils && ./install_server.sh
cd ../../
rm -rf redis
fi