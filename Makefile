.PHONY = build

SOURCES := $(shell find src -name '*.java')

build:
	@mkdir -p out
	javac --enable-preview -encoding UTF-8 -d out $(SOURCES) --release 25
