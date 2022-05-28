import 'bootstrap'
import { initialiseForm, filter, startNode } from './js/nodes'
import * as navActions from './js/navActions'
import { checkFields } from './js/formActions'

import 'bootstrap/dist/css/bootstrap.css'
import './css/style.scss'
import '@fortawesome/fontawesome-free/css/all.css'

// display settings for the nav
document.querySelector('#new').style.display = "none"
document.querySelector('#search').style.display = "none"

document.querySelector('#navHome').addEventListener('click', navActions.showSectionHome, false)
document.querySelector('#navNew').addEventListener('click', navActions.showSectionNew, false)
document.querySelector('#navSearch').addEventListener('click', navActions.showSectionSearch, false)

init()

// GET logo for the site

async function init() {
    await initialiseForm()
}

// form actions
document.querySelector('#addNode').addEventListener('submit', startNode, false)
document.querySelector('#filter').addEventListener('input', filter, false)
document.querySelector('#matchCase').addEventListener('input', filter, false)