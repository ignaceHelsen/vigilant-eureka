export class Node {
    constructor(id, name) {
        this._id = id
        this._name = name
    }

    // Properties 
    get id() {
        return this._id
    }

    get name() {
        return this._name
    }

    toString() {
        return JSON.stringify({
            name: _name,
            id: _id,
        })
    }
}