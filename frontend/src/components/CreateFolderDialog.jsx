import React, {Component} from 'react'

const axios = require('axios');

class CreateFolderDialog extends Component {
	constructor(props) {
		super(props);
		this.state = {
			props : props
		}
	}

	sendNewFolder(folder) {
		axios.post('http://localhost:8080/folder/create',
			{
				'folderName' : folder,
				'folderPath' : this.state.props.folderPath
			})
			.then(response => {
				console.log(response.data)
			})
			.catch(error => console.log(error));
	}

	render() {
		return (
			<div className="create-folder-dialog">
				<div className="dialog-header">
					<button
						className="prev-button"
						onClick={this.state.props.closeDialog}
					>
						<i className="fas fa-chevron-left"></i>
					</button>
					<i className="fas fa-folder"></i>
					<span>Create new Folder</span>
				</div>
				<div 
					className="container"
					style={{ textAlign : 'center' }}
				>
			        <div className="input-field col s6">
			          	<input 
			          		placeholder="Name"
			          		id="folder-name"
			          		type="text"
			          	></input>
			        </div>
			        <button 
			        	className="ok-button"
			        	onClick={() => this.sendNewFolder(document.getElementById('folder-name').value)}
			        >OK</button>
		        </div>
			</div>
		);
	}
};

export default CreateFolderDialog;