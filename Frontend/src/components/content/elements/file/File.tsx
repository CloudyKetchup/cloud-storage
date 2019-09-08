import React from 'react';

import EntityComponent, {EntityProps} from '../entity/EntityComponent';

const contextMenuListener = async (e: MouseEvent, obj: File) => {
	e.preventDefault();
	obj.setState({
		contextMenuShow  : true,
		contextMenuStyle : {
			top : e.y - 70,
			left: e.x - 275
		}
	});

	obj.props.parent.setState({ disableContextMenu : true });

	window.addEventListener('click', () => windowClickListener(obj), false);
};

const windowClickListener = async (obj: File) => {
	obj.setState({ contextMenuShow : false });

	obj.props.parent.setState({ disableContextMenu : false });
};

export default class File extends EntityComponent<EntityProps> {

	componentDidMount = () => {
		const div = document.getElementById(`file-${this.props.data.id}`);

		if (div !== null) div.addEventListener('contextmenu', e => contextMenuListener(e, this), false);
	};

	componentWillUnmount = () => {
		const div = document.getElementById(`file-${this.props.data.id}`);

		if (div !== null) div.removeEventListener('contextmenu', e => contextMenuListener(e, this), false);
	};

	render = () => (
		<div
			className="entity"
			key={this.props.data.path}
			id={`file-${this.props.data.id}`}
			style={{ height : "unset" }}
		>
			{this.contextMenu(this.props.data, this.props.handleAction, this.props.mainParent)}
			<div className="file-icon">
				<i className="fas fa-file"/>
			</div>
			<div className="file-name">
				<span>{this.name(this.props.data.name)}</span>
			</div>
		</div>
	);
}
