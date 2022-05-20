export class Mission {
    constructor(object) {
        this._id = object.id
        this._name = object.name
        this._logo = object.logo
        this._crewSize = object.crewSize
        this._launchdate = object.launchdate
        this._operator = object.operator
    }

    // Properties 
    get id() {
        return this._id
    }

    get name() {
        return this._name
    }

    get logo() {
        return this._logo
    }

    get crewSize() {
        return this._crewSize
    }

    get launchDate() {
        return this._launchdate
    }

    get operator() {
        return this._operator
    }

    toString() {
        return JSON.stringify({
            name: _name,
            crewSize: _crewSize,
            launchDate: _launchdate,
            operator: _operator,
        })
    }
}