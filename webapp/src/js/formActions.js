import { postNode } from "./restclient"
import { Node } from './Node'

export function checkFields() {
    const name = document.querySelector('#name').value

    document.querySelector('#nameValidation').innerText = ''
    document.querySelector('#globalValidation').innerText = ''


    let validation = true

    if (!checkName(name)) {
        validation = false
        document.querySelector('#nameValidation').innerText = 'Name not valid'
    }

    if (validation === true) {
        postNode({
            name
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