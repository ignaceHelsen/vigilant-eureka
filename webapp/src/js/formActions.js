import { postMission } from "./restclient"
import { Mission } from './Mission'

export function checkFields() {
    const name = document.querySelector('#name').value
    const crewSize = document.querySelector('#crewSize').value
    const launchdate = document.querySelector('#launchdate').value
    const operator = document.querySelector('#operator').value


    document.querySelector('#nameValidation').innerText = ''
    document.querySelector('#crewSizeValidation').innerText = ''
    document.querySelector('#launchdateValidation').innerText = ''
    document.querySelector('#operatorValidation').innerText = ''
    document.querySelector('#globalValidation').innerText = ''


    let validation = true

    if (!checkName(name)) {
        validation = false
        document.querySelector('#nameValidation').innerText = 'Name not valid'
    }

    if (!checkCrewSize(crewSize)) {
        validation = false
        document.querySelector('#crewSizeValidation').innerText = 'Crew size not valid'
    }

    if (!checkLaunchDate(launchdate)) {
        validation = false
        document.querySelector('#launchdateValidation').innerText = 'Launchdate not valid'
    }

    if (!checkOperator(operator)) {
        validation = false
        document.querySelector('#operatorValidation').innerText = 'Operator not valid'
    }

    if (validation === true) {
        postMission({
            name, crewSize, launchdate, operator
        })
    }
    else {
        document.querySelector('#globalValidation').innerText = 'Some field(s) may not be valid'
    }
}

function checkName(name) {
    if (name.length === 0) return false

    return true

}

function checkCrewSize(size) {
    if (size.length === 0) return false
    if (size < 0) return false

    return true
}

function checkLaunchDate(date) {
    date = new Date(date)
    if (date.getFullYear() < 1950) return false

    return true
}

function checkOperator(operator) {
    if (operator.toLowerCase() === 'nasa') return true
    if (operator.toLowerCase() === 'esa') return true
    if (operator.toLowerCase() === 'roskosmos') return true
    if (operator.toLowerCase() === 'soviet space program') return true

    return false
}