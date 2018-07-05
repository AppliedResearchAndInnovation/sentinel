from subprocess import call

print("Execute run.py")
call(["mvn", "clean"])
call(["mvn", "compile"])


#call(["mvn", "exec:java", "-Dexec.args='train FanShare-ID-train'"])
#call(["mvn", "exec:java", "-Dexec.args='train train'"])
call(["mvn", "exec:java", "-Dexec.args='train train-short'"])

#call(["mvn", "exec:java", "-Dexec.args='eval FanShare-ID-test'"])
#call(["mvn", "exec:java", "-Dexec.args='eval Tweet2013-test'"])
#call(["mvn", "exec:java", "-Dexec.args='eval test-short'"])
