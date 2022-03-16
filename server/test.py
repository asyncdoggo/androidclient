import json

mydict  = {
    "1":{
        "text":"this is a message",
        "user":"root"
    }
}
open("messages.json", 'a').close()

with open ("messages.json", 'r') as file:
    messages = json.load(file)
with open("messages.json", 'w') as file:
    messages["1"] = {"text":"hello","user":"a"}
    json.dump(messages,file)