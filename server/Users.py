import uuid


class Users:
    def __init__(self, username, name, lname):
        self.username = username
        self.key = uuid.uuid4().int
        self.name = name
        self.lname = lname
        data = [self.key,self.name,self.lname,self.username]
        print(data)

    def get(self):
        return self.username

    def getkey(self):
        return self.key

    def getunamebykey(self, key):
        if self.key == key:
            return self.username
