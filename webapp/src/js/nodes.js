import * as restClient from './restclient'
import { Node } from './Node'

let nodes = [];

let socket = new WebSocket('ws://localhost:5001/nodes')

export async function initialiseForm() {
    try {
        restClient.startNode();
        let json = await restClient.getNodes()

        for (let i in json)

            showNodes(nodes)
        showNodesSearch(nodes)
        showNamingServer();
    }
    catch (error) {
        namingServerOffline()
        showError('Node not found.')
    }
}

socket.onmessage = function (event) {
    initialiseForm()
}

socket.onclose = function (event) {
    if (event.wasClean) {
        console.log('Connection successfully closed.')
    } else {
        console.log('Connection unexpectedly closed.')
    }
};

socket.onerror = function (error) {
    console.log(`Websocket error ${error}`)
};

function showNamingServer() {
    // show naming server
    const namingServer = document.querySelector('#namingServer')
    namingServer.innerHTML = `<h1><i class="text-success fa fa-check-circle"></i> Naming server</h1>`
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
                    <h3 class="mt-2 text-center text-success">${node.id}</h2>
                </div>
                <div id="nodeName" class="col-12">
                    <h3 class="mt-2 text-center">${node.name}</h3>
                </div>
                <div id="shutdown" class="${node.id} col-12">
                    <h3 class="mt-2 text-center"><i class="text-danger fa fa-stop-circle"></i></h3>
                </div>
            </div>
        </div>
      `
}
async function showInfoOfNode(event) {
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

    try {
        let config = await restClient.getConfigFromNode(node_uri)
        showConfig(config);
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

function showConfig(config) {
    const element = document.querySelector('#config')
    const tbody = element.querySelector('tbody')
    tbody.innerHTML = `<div class="card bg-dark">
                            <div class="row">
                            <div class="col-12">
                                <p class="mt-2">Next node: ${config.nextNode}</p>
                            </div>
                            <div class="col-12">
                                <p class="mt-2">Previous node: ${config.previousNode}</p>
                            </div>
                            </div>
                        </div>
                    `
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
        nodes[i].addEventListener('click', showInfoOfNode, false)
    }

    const node = document.querySelector('#shutdown')
    node.addEventListener('click', shutdown, false)
}

async function shutdown(event) {
    const id = event.target.classList[0];
    const node_uri = nodes.filter(n => n.id == id)[0].name;

    await restClient.shutdownNode(node_uri)
}

export function showError(text) {
    const notFound = document.querySelector('#notFound');
    notFound.innerHTML = text
}

function namingServerOffline() {
    // remove naming server
    const namingServer = document.querySelector('#namingServer')
    namingServer.innerHTML = `<h1><i class="text-danger fa fa-check-circle"></i> Naming server</h1>`
}