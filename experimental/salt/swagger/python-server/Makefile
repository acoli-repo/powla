# for Docker development, not for production mode
start:
		@# for restarting, kill the xterm with the Docker manually

		docker build -t acoli/tordf .
		xterm -e docker run -p 8080:8080 acoli/tordf &
		# docker -H tcp://172.17.0.2:8080 run -p 8080:8080 acoli/tordf
		# curl -X "DELETE" 'http://172.17.0.2:8080/data/resource/myid1'

		@# doesn't work because they require the files to be named *.txt
		# curl -X 'POST' 'http://172.17.0.2:8080/data/file/txt1' -F 'file=@'`realpath README.md` -F 'importer=TextImporter'

test: start
		curl -X 'POST' 'http://172.17.0.2:8080/data/file/ex1' -F 'file=@'`realpath ../../samples/swc.zip` -F "format=POWLA" -F 'importer=ExmaraldaImporter'

clean:
		docker rmi -f acoli/tordf
		- docker rmi -f $$(docker images -f "dangling=true" -q)
