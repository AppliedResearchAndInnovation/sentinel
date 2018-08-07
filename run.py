from subprocess import call

print("Execute run.py")
call(["mvn", "clean"])
call(["mvn", "compile"])


#call(["mvn", "exec:java", "-Dexec.args='train AndrewLabel_train'"])
#call(["mvn", "exec:java", "-Dexec.args='train train'"])
#call(["mvn", "exec:java", "-Dexec.args='train train-short'"])

#call(["mvn", "exec:java", "-Dexec.args='eval AndrewLabel_test'"])
call(["mvn", "exec:java", "-Dexec.args='eval test_example'"])
#call(["mvn", "exec:java", "-Dexec.args='eval Tweet2013-test'"])
#call(["mvn", "exec:java", "-Dexec.args='eval train-short'"])
