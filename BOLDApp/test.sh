#!/bin/bash

ant -v test
cat out/reports/tests/*.txt
