import { useState } from 'react';

function ControlledForm() {

  const [data, setData] = useState({
    name: '',
    email: ''
  });

  const { name, email } = data;

  function handleChange(event) {
    const { name, value } = event.target;

    setData({
      ...data,
      [name]: value
    });
  }

  function handleSubmit(event) {
    event.preventDefault();
    console.log(data);
  }

  return (
    <form onSubmit={handleSubmit}>
      
      <label>
        Name:
        <input type="text" name="name" value={name} onChange={handleChange} />
      </label>
      <label>
        Email:
        <input type="email" name="email" value={email} onChange={handleChange} />
      </label>
      <button type="submit">Submit</button>

    </form>
  );
}

export default ControlledForm;