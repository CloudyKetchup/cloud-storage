import React, { Component } from 'react';

import { Entity } 		from '../../model/entity/Entity';
import App 				from '../../App';
import ContentContainer from '../ContentContainer/ContentContainer';
import EntityContextMenu 		from "../EntityContextMenu/EntityContextMenu";

export type EntityProps = {
	parent: ContentContainer
	mainParent: App
	handleAction: (action: string) => void
}

export default abstract class EntityComponent<EntityProps> extends Component<EntityProps> {
	state = {
		contextMenuShow : false,
		contextMenuStyle : {
			top: '',
			left: ''
		}
	};

	contextMenu = (data: Entity, handleAction: (action: string) => void, app : App) => {
		if (this.state.contextMenuShow) {
			return 	<EntityContextMenu
				parent={data}
				action={handleAction}
				onStart={() => app.setState({ elementSelected : data })}
				style={this.state.contextMenuStyle}
			/>
		}
	};

	name = (name: string) => {
		return name.length > 14 ? `${name.substring(0, 13)}...` : name;
	};
}
