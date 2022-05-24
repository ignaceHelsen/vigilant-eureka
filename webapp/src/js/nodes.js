import * as restClient from './restclient'
import { BASE_NODES_URL } from './restclient'
import { Node } from './Node'

export async function initialiseFirstNode() {
    try {
        let node = await restClient.getNode(1)
        node = new Node(node)
        showNode(node)
    }
    catch (e) {
        console.log(e)
    }
}

export async function initialiseForm() {
    try {
        let json = await restClient.getNodes()
        let nodes = []

        for(let i in json)
            nodes.push(new Node(i,json[i]));

        showNodes(nodes)
        showNodesSearch(nodes)
    }
    catch (error) {
        showError('Node not found.')
    }
}

function showNode(node) {
    if (node != undefined) {
        const formNode = document.querySelector("#formnode");
        formnformNodeode.innerHTML = ''
        formNode.innerHTML +=
            `
            <div class="row">
                <div class="col-md-3 col-sm-12 text-center">
                    <h2 id="nodeId">${node.id}</h2>
                </div>
                <div class="col-md-3 col-sm-12 text-center">            
                    <h2 id="nodeName">${node.name}</h2>
                </div>
            </div >
            `
    }
}

function showNodes(nodes) {
    for (let i = 0; i < nodes.length; i++) {
        addNode(nodes[i])
    }

    initialiseEventListeners()
}

function addNode(node) {
    const formNodes = document.querySelector('#formNodes');
    formNodes.innerHTML +=
        `
        <div class="card bg-dark" col-sm-6 col-lg-3 col-xl-4 mb-5 mt-5">
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

export function initialiseEventListeners() {
    const images = document.getElementsByClassName('nodeLogo');

    for (let i = 0; i < images.length; i++) {
        images[i].addEventListener('click', showNodeById, false);
    }
}

async function showNodeById(event) {
    const id = (event.target.id)

    try {
        let node = await restClient.getNode(id)
        node = new Node(node)

        showNode(node)
    }
    catch (e) {
        showError('Node not found.')
    }
}

export async function filter(event) {
    let text = document.querySelector('#filter').value
    let matchCase = document.querySelector('#matchCase')

    try {
        let json = await restClient.getNodes()
        let nodes = []

        for(let i in json)
            nodes.push(new Node(i,json[i]));


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
    const tableSearch = document.querySelector('#tableSearch');
    const tbody = tableSearch.querySelector('tbody')
    tbody.innerHTML = ''
    for (let i = 0; i < nodes.length; i++) {
        addNodeToTable(nodes[i])
    }

    document.querySelector('#count').innerHTML = nodes.length
}

function addNodeToTable(node) {
    const tableSearch = document.querySelector('#tableSearch');
    const tbody = tableSearch.querySelector('tbody')
    tbody.innerHTML +=
        `<tr>
            <th scope="row">${node.id}</th>
            <td>${node.id}</td>
            <td>${node.name}</td>
        </tr>
        `
}


export function showError(text) {
    const notFound = document.querySelector('#notFound');
    notFound.innerHTML = text
}