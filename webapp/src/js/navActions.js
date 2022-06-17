export function showSectionHome() {
    document.querySelector('#home').style.display = "block"
    document.querySelector('#new').style.display = "none"
    document.querySelector('#search').style.display = "none"

    document.querySelector('#navHome').classList.add('active')
    document.querySelector('#navNew').classList.remove('active')
    document.querySelector('#navSearch').classList.remove('active')
}

export function showSectionNew() {
    document.querySelector('#home').style.display = "none"
    document.querySelector('#new').style.display = "block"
    document.querySelector('#search').style.display = "none"

    document.querySelector('#navHome').classList.remove('active')
    document.querySelector('#navNew').classList.add('active')
    document.querySelector('#navSearch').classList.remove('active')
}

export function showSectionSearch() {
    document.querySelector('#home').style.display = "none"
    document.querySelector('#new').style.display = "none"
    document.querySelector('#search').style.display = "block"

    document.querySelector('#navHome').classList.remove('active')
    document.querySelector('#navNew').classList.remove('active')
    document.querySelector('#navSearch').classList.add('active')
}