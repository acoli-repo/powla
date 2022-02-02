# for Docker development, not for production mode
start:
		@# for restarting, kill the xterm with the Docker manually

		docker build -t acoli/tordf .
		xterm -e docker run -p 8080:8080 acoli/tordf &
		# docker -H tcp://172.17.0.2:8080 run -p 8080:8080 acoli/tordf
		# curl -X "DELETE" 'http://172.17.0.2:8080/data/resource/myid1'

		@# doesn't work because they require the files to be named *.txt
		# curl -X 'POST' 'http://172.17.0.2:8080/data/file/txt1' -F 'file=@'`realpath README.md` -F 'importer=TextImporter'

		curl -X 'POST' 'http://172.17.0.2:8080/data/file/ex1' -F 'file=@'`realpath ../../samples/swc.zip` -F 'importer=ExmaraldaImporter'

reset:
	@# cleanup, replaced by xterm command in start: close the terminal to shut down ;)
	# !(echo | killall acoli/tordf ) &
	# !(echo | killall docker   ) &
	# !(docker stop $(docker ps -aq)  ) &
	# ! fuser -k 8080/tcp
	#
	# @# I'm getting really desparate here ...
	# for p in `sudo lsof -i -P -n | grep 8000 | cut -f 2 -d ' '`; do sudo kill -9 $p; done
	# sudo service docker stop
	# sudo service docker start

clean:
		docker rmi -f acoli/tordf
		!docker rmi -f $$(docker images -f "dangling=true" -q)
