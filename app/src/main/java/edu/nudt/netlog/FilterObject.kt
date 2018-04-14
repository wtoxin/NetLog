package edu.nudt.netlog


class FilterObject {
    var app = ""
    var src = ""
    var dst = ""
    var sPort = -1
    var dPort = -1
    var tcp = false

    constructor(app: String, src: String, dst: String, sPort: Int, dPort: Int, tcp: Boolean) {
        this.app = app
        this.src = src
        this.dst = dst
        this.sPort = sPort
        this.dPort = dPort
        this.tcp = tcp
    }

    override fun equals(obj: Any?): Boolean {
        if (obj is FilterObject) {
            return ((obj.src == src && obj.dst == dst && obj.sPort == sPort && obj.dPort == dPort) ||
                    (obj.src == dst && obj.sPort == dPort && obj.dst == src && obj.dPort == sPort)) &&
                    obj.app == app
        }
        return false
    }

    override fun toString(): String {
        return if (!tcp)
            "(ip.addr == $src && ip.addr == $dst)"
        else
            "(ip.addr == $src && ip.addr == $dst && tcp.port == $sPort && tcp.port == $dPort)"
    }
}
