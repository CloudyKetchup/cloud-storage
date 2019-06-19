import React, { Component } from 'react';

export default class DragAndDrop extends Component {
	state = { dragging: false };

	dropRef = React.createRef();

	dragCounter = 0;

	handleDrag = e => {
		e.preventDefault();
		e.stopPropagation();
	};

	handleDragIn = e => {
		e.preventDefault();
		e.stopPropagation();

		this.dragCounter++;

		if (e.dataTransfer.items && e.dataTransfer.items.length > 0) {
			this.setState({dragging: true})
		}
	};

	handleDragOut = e => {
		e.preventDefault();
		e.stopPropagation();

		this.dragCounter--;

		if (this.dragCounter === 0) {
			this.setState({dragging: false})
		}
	};

	handleDrop = e => {
		e.preventDefault();
		e.stopPropagation();

		this.setState({dragging: false});

		if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
			this.props.handleDrop(e.dataTransfer.files);

			e.dataTransfer.clearData();
			this.dragCounter = 0
		}
	};

	componentDidMount() {
		let div = this.dropRef.current;

		div.addEventListener('dragenter', this.handleDragIn);
		div.addEventListener('dragleave', this.handleDragOut);
		div.addEventListener('dragover', this.handleDrag);
		div.addEventListener('drop', this.handleDrop);
	}

	componentWillUnmount() {
		let div = this.dropRef.current;

		div.removeEventListener('dragenter', this.handleDragIn);
		div.removeEventListener('dragleave', this.handleDragOut);
		div.removeEventListener('dragover', this.handleDrag);
		div.removeEventListener('drop', this.handleDrop);
	}

	render() {
		return (
			<div ref={this.dropRef}>
				{this.state.dragging &&
				<div className={this.props.className} style={{ display : this.state.dragging ? 'block' : 'none' }}>
					<div className="drag-and-drop-text">
						<div>Drop it here to upload</div>
					</div>
				</div>
				}
				{this.props.children}
			</div>
		)
	}
}