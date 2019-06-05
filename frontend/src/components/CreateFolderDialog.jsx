import React, {Component} from 'react'

class CreateFolderDialog extends Component {

	render() {
		return (
			<div className="create-folder-dialog">
				<div className="dialog-header">
					<button
						className="prev-button"
						onClick={this.props.closeDialog}
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
			        	onClick={() => this.props.sendFolder(document.getElementById('folder-name').value)}
			        >OK</button>
		        </div>
			</div>
		);
	}
};

export default CreateFolderDialog;