# Makefile for ttc2ttf

PLATFORM := OSX
MY_NAME := ttc2ttf

export MACOSX_DEPLOYMENT_TARGET=10.6

export CC := gcc
export CXX := g++
export JAVAC := javac

export ERASE_FILE ?= rm -f
export ERASE_DIR ?= rm -r
export MAKE_DIR ?= mkdir -p
export COPY_FILE ?= cp
export MAKE_LIB ?= g++ -dynamiclib -o
export INDEX_LIB ?= @echo
export MAKE ?= make

export ARCH ?= -arch x86_64

export SCRATCH_DIR := scratch
export LIB_PATH := scratch
export LIB_OUTPUT := ../../lib
export BIN_OUTPUT := ../../bin


RELEASE_CXXFLAGS := -Wall -O3 -force_cpusubtype_ALL -Duse_namespace
#RELEASE_CXXFLAGS := $(RELEASE_CXXFLAGS) -isysroot /Developer/SDKs/MacOSX10.5.sdk
RELEASE_CXXFLAGS := $(RELEASE_CXXFLAGS) $(ARCH)
RELEASE_CXXFLAGS := $(RELEASE_CXXFLAGS) -mmacosx-version-min=$(MACOSX_DEPLOYMENT_TARGET)

RELEASE_LDFLAGS := -Wall 
#RELEASE_LDFLAGS := $(RELEASE_LDFLAGS) -Wl,-syslibroot,/Developer/SDKs/MacOSX10.5.sdk 
RELEASE_LDFLAGS := -Wl
RELEASE_LDFLAGS := $(RELEASE_LDFLAGS) $(ARCH)
RELEASE_LDFLAGS := $(RELEASE_LDFLAGS) -mmacosx-version-min=$(MACOSX_DEPLOYMENT_TARGET)

export CXXFLAGS := $(RELEASE_CXXFLAGS)
export LDFLAGS := $(RELEASE_LDFLAGS)

OUTPUT_DIR := $(BIN_OUTPUT)
FINAL_TARGET := $(OUTPUT_DIR)/$(MY_NAME)

SOURCE_DIRS := .
LIB_NAMES := 
INCLUDE_NAMES := 
EXT_INCLUDES := 
#-I/System/Library/Frameworks/JavaVM.framework/Home/include -I.
EXT_LIBS := 
FRAMEWORKS :=

CPP_SOURCES := $(foreach dir, $(SOURCE_DIRS), $(wildcard $(dir)/*.cpp))
C_SOURCES := $(foreach dir, $(SOURCE_DIRS), $(wildcard $(dir)/*.c))
HEADERS := $(foreach dir, $(SOURCE_DIRS), $(wildcard $(dir)/*.h))

INCLUDES := $(EXT_INCLUDES)
INCLUDES := $(INCLUDES) $(foreach dir, $(SOURCE_DIRS), -I$(dir))
INCLUDES := $(INCLUDES) $(foreach dir, $(INCLUDE_NAMES), -I$(LIB_PATH)/$(dir))
INCLUDES := $(INCLUDES) $(foreach dir, $(LIB_NAMES), -I$(LIB_PATH)/lib$(dir))

LIBS := $(EXT_LIBS) $(FRAMEWORKS)

OBJECTS := $(patsubst %.cpp, $(SCRATCH_DIR)/%.cpp.o, $(CPP_SOURCES))
OBJECTS := $(OBJECTS) $(patsubst %.c, $(SCRATCH_DIR)/%.c.o, $(C_SOURCES))


.PHONY: all
all: _make_some_room_ compile


compile: 
	$(MAKE_DIR) $(OUTPUT_DIR)
	$(CXX) -o $(FINAL_TARGET) $(CPP_SOURCES) $(CXXFLAGS) $(INCLUDES)
	@echo done


.PHONY: _make_some_room_
_make_some_room_:
	@echo
	@echo
	@echo Making $(MY_NAME)
	@echo
	@echo $(CPP_SOURCES)

.PHONY: clean
clean:
	$(ERASE_DIR) $(SCRATCH_DIR)

.PHONY: _clean_me_/%
_clean_me_/%:
	$(ERASE_FILE) $*
