import React, { Component } from 'react';

import ContextMenu 	from './ContextMenu'

export default class Folder extends Component {
	state={ contextMenuShow : false }

	componentDidMount() {
		document.getElementById(`folder-${this.props.data.id}`).addEventListener('contextmenu', e => {
			e.preventDefault();
			this.setState({ contextMenuShow : true });

			this.props.parent.setState({ disableContextMenu : true });
		});

		window.addEventListener('click', () => {
			this.setState({ contextMenuShow : false });

			this.props.parent.setState({ disableContextMenu : false });
		}, false);
	}

	contextMenu() {
		if (this.state.contextMenuShow) {
			return 	<ContextMenu
					action={action => this.props.handleAction(action)}
					onStart={() => this.props.mainParent.setState({ elementSelected : undefined })}
					/>
		}
	}

	render() {
		return (
			<div
				className="folder"
				key={this.props.data.id}
				id={`folder-${this.props.data.id}`}
				onClick={this.props.whenClicked}
			>
				{this.contextMenu()}
				<div className="folder-icon">
					<i className="fas fa-folder"/>
				</div>
				<div className="folder-name">
					<span>{this.props.data.name}</span>
				</div>
			</div>
		);
	}
}