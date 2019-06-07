import React, { Component } from 'react';

import icon	 		from '../images/folder.png'
import ContextMenu 	from './ContextMenu'

export default class Folder extends Component {
	constructor() {
		super();
		this.state={ contextMenuShow : false }
	}

	componentDidMount() {
		document.getElementById(`folder-${this.props.id}`).addEventListener('contextmenu', e => {
			e.preventDefault();
			this.setState({ contextMenuShow : true })	
		});

		window.addEventListener('click', () => this.setState({ contextMenuShow : false }), false);
	}

	render() {
		return (
			<div
				className="folder"
				id={`folder-${this.props.id}`}
				onClick={this.props.whenClicked}
				style={{ 
				    width	: '100px',
				    height	: '100px'
			  	}}
			>
				{this.state.contextMenuShow
					? <ContextMenu
						action={action => this.props.handleAction(action)}
						onStart={() => this.props.parent.setState({ elementSelected : undefined })}
						parent={this.props.parent}/>
					: undefined}
				<div>
					<img 
						src={icon} 
						style={{
							height		: '40px',
							width		: '50px',
							objectFit	: 'cover'
						}} 
						alt="???"
					/>	
				</div>
				<div>
					<span>{this.props.name}</span>
				</div>
			</div>
		);
	}
}