name = java-deptools
dir = org/fedoraproject/javadeptools
classpath = $(shell build-classpath $(shell cat deps))
jar = $(name).jar

all: $(jar)

$(dir):
	@mkdir -p $(shell dirname $(dir))
	@ln -sf ../.. $(dir)

$(jar): $(dir)
	javac -cp $(classpath) *.java
	jar cfm $@ manifest $(dir)/*.class

clean: $(dir)
	@rm -f $(dir) $(jar) *.class
	@rmdir -p $(shell dirname $(dir))

.PHONY: all $(jar) clean
