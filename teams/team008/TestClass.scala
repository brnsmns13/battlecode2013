package dumb

import battlecode.common._

class TestClass(val start_c: Int) {
    var count: Int = start_c

    def inc(dx: Int) {
        count = count + dx
    }
}
