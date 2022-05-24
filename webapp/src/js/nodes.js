import * as restClient from './restclient'
import { NAMING_SERVER_URL } from './restclient'
import { Node } from './Node'

let nodes = [];

export async function initialiseForm() {
    try {
        let json = await restClient.getNodes()
        
        for(let i in json)
            nodes.push(new Node(i,json[i]));

        showNodes(nodes)
        showNodesSearch(nodes)
        showNamingServer();
    }
    catch (error) {
        showError('Node not found.')
    }
}

function showNamingServer() {
    // show naming server
    const namingServer = document.querySelector('#namingServer')
    namingServer.innerHTML = `<i class="text-success fa-solid fa-circle-check"></i><h1>Naming server</h1>`
}


function showNodes(nodes) {
    for (let i = 0; i < nodes.length; i++) {
        addNode(nodes[i])
    }
}

function addNode(node) {
    const formNodes = document.querySelector('#formNodes');
    formNodes.innerHTML +=
        `
        <div class="card bg-dark">
            <div class="row">
            <div id="nodeId" class="col-12">
                <h3 class="mt-2 text-center">${node.id}</h2>
            </div>
            <div id="nodeName" class="col-12">
                <h3 class="mt-2 text-center">${node.name}</h2>
            </div>
            </div>
        </div>
      `
}
async function showFilesOfNode(event) {
    const id = event.target.classList[0];
    const node_uri = nodes.filter(n => n.id == id)[0].name;

    try {
        let files = await restClient.getLocalFilesFromNode(node_uri)
        showLocalFiles(files);
    }
    catch (e) {
        showError('Node not found.')
    }

    try {
        let files = await restClient.getReplicatedFilesFromNode(node_uri)
        showReplicaFiles(files);
    }
    catch (e) {
        showError('Node not found.')
    }
}

function showLocalFiles(files) {
    const element = document.querySelector('#localFiles')
    const tbody = element.querySelector('tbody')
    tbody.innerHTML = ''

    files.forEach(f => {
        tbody.innerHTML += `<tr>
                                <th scope="row">${f}</th>
                            </tr>`
    })
}

function showReplicaFiles(files) {
    const element = document.querySelector('#replicaFiles')
    const tbody = element.querySelector('tbody')
    tbody.innerHTML = ''

    files.forEach(f => {
        tbody.innerHTML += `<tr>
                                <th scope="row">${f}</th>
                            </tr>`
    })
}

export function filter(event) {
    let text = document.querySelector('#filter').value
    let matchCase = document.querySelector('#matchCase')

    try {
        let nodeList;

        if (text != null) {
            if (matchCase.checked) {
                nodeList = nodes.filter(n => n.name.includes(text) || n.id.toString().includes(text))
            }
            else {
                nodeList = nodes.filter(n => n.name.toLowerCase().includes(text.toLowerCase()) || n.id.toString().includes(text.toLowerCase()))
            }
        }

        showNodesSearch(nodeList)
    }
    catch (e) {
        showError('Nodes not found.')
    }
}

function showNodesSearch(nodes) {
    const tableSearch = document.querySelector('#tableSearch')
    const tbody = tableSearch.querySelector('tbody')
    tbody.innerHTML = ''
    for (let i = 0; i < nodes.length; i++) {
        addNodeToTable(nodes[i])
    }

    document.querySelector('#count').innerHTML = nodes.length

    initialiseEventListeners()
}

function addNodeToTable(node) {
    const tableSearch = document.querySelector('#tableSearch')
    const tbody = tableSearch.querySelector('tbody')
    tbody.innerHTML +=
        `<tr class="tableNode">
            <th class="${node.id} scope="row">${node.id}</th>
            <td class="${node.id} scope="row">${node.name}</td>
        </tr>
        `
}

function initialiseEventListeners() {
    const nodes = document.getElementsByClassName('tableNode');

    for (let i = 0; i < nodes.length; i++) {
        nodes[i].addEventListener('click', showFilesOfNode, false);
    }
}

export function showError(text) {
    const notFound = document.querySelector('#notFound');
    notFound.innerHTML = text
    // remove naming server
    const namingServer = document.querySelector('#namingServer')
    namingServer.innerHTML = `<h1><i class="text-danger fa-solid fa-circle-check"></i>Naming server</h1>`
}