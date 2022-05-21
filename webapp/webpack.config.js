const HtmlWebpackPlugin = require('html-webpack-plugin')
const path = require('path')

module.exports = {
  entry: './src/app.js',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, 'dist')
  },
  devtool: 'source-map',
  mode: 'development',
  module: {
    rules: [
      { test: /\.s?css$/, use: ['style-loader', 'css-loader','sass-loader' ]},
      { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, use: [{loader: 'file-loader' }]},
      { test: /\.(woff|woff2)$/, use: [{loader: 'url-loader?prefix=font/&limit=5000' }]},
      { test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, use: [{loader: 'url-loader?limit=10000&mimetype=application/octet-stream'}]},
      { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, use: [{loader: 'url-loader?limit=10000&mimetype=image/svg+xml'}]}
    ]
  },
  plugins: [
    new HtmlWebpackPlugin({
      filename: 'index.html',
      template: 'src/html/index.html'
    })
  ],
  devServer: {
    static: path.resolve(__dirname, 'dist'),
    port: 9000
  }
}
