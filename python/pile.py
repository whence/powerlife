class Pile:
    def __init__(self, factory, count):
        self.factory = factory
        self.remaining = count
        self.sample = self.factory()

    def empty(self):
        return self.remaining <= 0

    def push(self):
        self.remaining += 1

    def pop(self):
        assert not self.empty, '{} pile is empty'.format(self.sample.name)
        self.remaining -= 1
        return self.factory()
